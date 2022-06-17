/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.api.stacks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import appeng.api.config.FuzzyMode;
import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
public class KeyCounterTest {

    KeyCounter itemList = new KeyCounter();

    /**
     * add should merge item stacks by adding stored/requestable counts, and setting craftable if it wasn't set before.
     */
    @Test
    public void testAdd() {
        itemList.add(diamondSword(100), 1);
        assertEquals(1, itemList.get(diamondSword(100)));

        itemList.add(diamondSword(100), 1);
        assertEquals(2, itemList.get(diamondSword(100)));

        itemList.add(diamondSword(100), -3);
        assertEquals(-1, itemList.get(diamondSword(100)));

        assertThat(itemList.keySet()).containsOnly(diamondSword(100));
    }

    /**
     * stacks for the same item, but different damage values should not be merged
     */
    @Test
    public void testAddDoesNotMergeAcrossDamageValues() {
        itemList.add(diamondSword(100), 1);
        itemList.add(diamondSword(99), 1);
        assertEquals(1, itemList.get(diamondSword(100)));
        assertEquals(1, itemList.get(diamondSword(99)));
        assertThat(itemList.keySet()).containsOnly(diamondSword(100), diamondSword(99));
    }

    /**
     * If the stack is not craftable, an empty stack is actually ignored.
     */
    @Test
    public void testRemoveZeros() {
        itemList.add(diamondSword(100), 0);
        itemList.removeZeros();
        assertThat(itemList.keySet()).containsOnly();
    }

    @Test
    public void testResetStatus() {
        itemList.add(diamondSword(100), 1);
        itemList.add(nameTag(), 1);
        assertEquals(2, itemList.size());
        assertThat(itemList.keySet()).containsOnly(diamondSword(100), nameTag());

        itemList.reset();

        assertThat(itemList.keySet()).containsOnly(diamondSword(100), nameTag());

        itemList.removeZeros();

        assertThat(itemList.keySet()).containsOnly(); // The list should now be empty
    }

    /**
     * Tests that iteration across multiple items and variations of those items works.
     */
    @Test
    public void testIterateAcrossMultipleItems() {
        // Add damaged variants of the same item, including NBT variants
        var sword1 = diamondSword(100);
        itemList.add(sword1, 1);
        var sword2 = diamondSword(50);
        itemList.add(sword2, 1);
        var sword3 = diamondSword(25);
        itemList.add(sword3, 1);
        var sword4 = diamondSword(100, "master sword");
        itemList.add(sword4, 1);
        // And a non-damagable item with different NBT
        var nameTag1 = nameTag();
        itemList.add(nameTag1, 1);
        var nameTag2 = nameTag("bob");
        itemList.add(nameTag2, 1);

        assertListContent(sword1, sword2, sword3, sword4, nameTag1, nameTag2);
    }

    @Test
    void testConcurrentModificationByAddingItemType() {
        var sword = diamondSword(100);
        itemList.add(sword, 1);
        var nameTag = nameTag();
        itemList.add(nameTag, 1);
        var craftingTable = AEItemKey
                .of(new ItemStack(Items.CRAFTING_TABLE));

        assertThrows(ConcurrentModificationException.class, () -> {
            var it = itemList.iterator();
            itemList.add(craftingTable, 1);
            it.forEachRemaining(x -> {
            });
        });
    }

    /**
     * Regression test for broken iterators that mutated state in {@see Iterator#hasNext}. This was the case for both
     * the top-level and sub-iterator.
     */
    @Test
    public void testIteratorHasNextDoesNotSkipItems() {
        itemList.add(diamondSword(100), 1);
        itemList.add(diamondSword(50), 1);

        var it = itemList.iterator();
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
    }

    @Test
    public void testGetFirstItemForEmptyList() {
        assertNull(itemList.getFirstEntry());
        assertNull(itemList.getFirstKey());
    }

    @Test
    public void testGetFirstItem() {
        var itemStack = diamondSword(100);
        itemList.add(itemStack, 1);
        // The order is no longer well defined w.r.t. the hashmap
        assertEquals(itemStack, itemList.getFirstKey());
        assertEquals(itemStack, itemList.getFirstEntry().getKey());
        assertEquals(1, itemList.getFirstEntry().getLongValue());
    }

    @Nested
    class FindFuzzyDamageableItems {

        // Swords to cover all durability values
        AEItemKey[] swords = new AEItemKey[101];
        // Filters for inverting the filter as needed
        AEItemKey undamagedFilter = diamondSword(100);
        AEItemKey damagedFilter = diamondSword(0);

        @BeforeEach
        void addItems() {
            for (var i = 0; i <= 100; i++) {
                swords[i] = diamondSword(i);
                assertEquals(i, getDurabilityPercent(swords[i]));
                itemList.add(swords[i], 1);
            }
        }

        @Test
        public void testIgnoreAllWithUndamagedFilter() {
            assertReturnedDurabilities(undamagedFilter, FuzzyMode.IGNORE_ALL, 0, 100);
        }

        @Test
        public void testIgnoreAllWithDamagedFilter() {
            assertReturnedDurabilities(damagedFilter, FuzzyMode.IGNORE_ALL, 0, 100);
        }

        @Test
        public void testPercent99WithUndamagedFilter() {
            assertReturnedDurabilities(undamagedFilter, FuzzyMode.PERCENT_99, 100, 100);
        }

        @Test
        public void testPercent99WithDamagedFilter() {
            assertReturnedDurabilities(damagedFilter, FuzzyMode.PERCENT_99, 0, 99);
        }

        @Test
        public void testPercent75WithUndamagedFilter() {
            assertReturnedDurabilities(undamagedFilter, FuzzyMode.PERCENT_75, 75, 100);
        }

        @Test
        public void testPercent75WithDamagedFilter() {
            assertReturnedDurabilities(damagedFilter, FuzzyMode.PERCENT_75, 0, 74);
        }

        @Test
        public void testPercent50WithUndamagedFilter() {
            assertReturnedDurabilities(undamagedFilter, FuzzyMode.PERCENT_50, 50, 100);
        }

        @Test
        public void testPercent50WithDamagedFilter() {
            assertReturnedDurabilities(damagedFilter, FuzzyMode.PERCENT_50, 0, 49);
        }

        @Test
        public void testPercent25WithUndamagedFilter() {
            assertReturnedDurabilities(undamagedFilter, FuzzyMode.PERCENT_25, 25, 100);
        }

        @Test
        public void testPercent25WithDamagedFilter() {
            assertReturnedDurabilities(damagedFilter, FuzzyMode.PERCENT_25, 0, 24);
        }

        private void assertReturnedDurabilities(AEItemKey filter, FuzzyMode fuzzyMode, int minDurabilityInclusive,
                int maxDurabilityInclusive) {
            var items = itemList.findFuzzy(filter, fuzzyMode);

            // Build a list of the durabilities that got returned
            var durabilities = items.stream()
                    .map(Map.Entry::getKey)
                    .map(this::getDurabilityPercent)
                    .sorted()
                    .collect(Collectors.toList());

            // Build a sorted list of the durabilities we expect
            List<Integer> expectedDurabilities = new ArrayList<>();
            for (var i = minDurabilityInclusive; i <= maxDurabilityInclusive; i++) {
                expectedDurabilities.add(getDurabilityPercent(swords[i]));
            }
            expectedDurabilities.sort(Integer::compare);

            assertEquals(expectedDurabilities, durabilities);
        }

        private int getDurabilityPercent(AEKey stack) {
            if (stack instanceof AEItemKey itemKey) {
                var is = itemKey.toStack();
                return (int) ((1.0f - is.getDamageValue() / (float) is.getMaxDamage()) * 100);
            } else {
                return 100;
            }
        }
    }

    @Test
    void testFindFuzzyForNormalItems() {
        var item1 = nameTag(null);
        itemList.add(item1, 1);
        var item2 = nameTag("name1");
        itemList.add(item2, 1);
        var item3 = nameTag("name2");
        itemList.add(item3, 1);
        // Add another item to ensure this is not returned
        itemList.add(AEItemKey.of(new ItemStack(Items.CRAFTING_TABLE)), 1);

        for (var fuzzyMode : FuzzyMode.values()) {
            var result = itemList.findFuzzy(nameTag(null), fuzzyMode);
            assertThat(result)
                    .extracting(Map.Entry::getKey)
                    .containsOnly(item1, item2, item3);
        }
    }

    /**
     * Unlike previous iterations of item lists in AE, KeyCounter will throw on null arguments.
     */
    @Nested
    class NullArguments {
        @Test
        void testFindFuzzy() {
            assertThrows(NullPointerException.class, () -> itemList.findFuzzy(null, FuzzyMode.PERCENT_99));
        }

        @Test
        void testFindPrecise() {
            assertThrows(NullPointerException.class, () -> itemList.get(null));
        }

        @Test
        void testAdd() {
            assertThrows(NullPointerException.class, () -> itemList.add(null, 1));
        }
    }

    private void assertListContent(AEItemKey... stacks) {
        assertEquals(stacks.length == 0, itemList.isEmpty(), "isEmpty");
        assertEquals(stacks.length, itemList.size());
        assertEquals(ImmutableSet.copyOf(stacks), ImmutableSet.copyOf(itemList.keySet()));
    }

    private AEItemKey diamondSword(int durabilityPercent) {
        return diamondSword(durabilityPercent, null);
    }

    private AEItemKey diamondSword(int durabilityPercent, String customName) {
        var is = new ItemStack(Items.DIAMOND_SWORD);
        if (customName != null) {
            is.setHoverName(Component.literal(customName));
        }
        var damage = (int) ((100 - durabilityPercent) / 100.0f * is.getMaxDamage());
        is.setDamageValue(damage);
        return AEItemKey.of(is);
    }

    // customName can be used to create items that differ in NBT
    private AEItemKey nameTag() {
        return nameTag(null);
    }

    private AEItemKey nameTag(String customName) {
        var is = new ItemStack(Items.NAME_TAG);
        if (customName != null) {
            is.setHoverName(Component.literal(customName));
        }
        return AEItemKey.of(is);
    }

}
