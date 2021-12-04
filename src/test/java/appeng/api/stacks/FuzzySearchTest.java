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

import appeng.api.config.FuzzyMode;
import appeng.util.BootstrapMinecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@BootstrapMinecraft
public class FuzzySearchTest {

    @Test
    void testOrderForDamagedItems() {
        // Diamond Sword @ 100% durability
        ItemStack undamagedSword = new ItemStack(Items.DIAMOND_SWORD);
        AEItemKey undamagedStack = AEItemKey.of(undamagedSword);

        // Unbreakable Diamond Sword @ 50% durability
        ItemStack unbreakableSword = new ItemStack(
                Items.DIAMOND_SWORD);
        unbreakableSword.setDamageValue(unbreakableSword.getMaxDamage() / 2);
        unbreakableSword.getOrCreateTag().putBoolean("Unbreakable", true);
        assertFalse(unbreakableSword.isDamageableItem());
        AEItemKey unbreakableStack = AEItemKey.of(unbreakableSword);

        // Unenchanted Diamond Sword @ 0% durability
        ItemStack damagedSword = new ItemStack(Items.DIAMOND_SWORD);
        damagedSword.setDamageValue(damagedSword.getMaxDamage());
        AEItemKey damagedStack = AEItemKey.of(damagedSword);

        // Create a list of stacks and sort by their natural order
        AEItemKey[] stacks = new AEItemKey[]{
                damagedStack, undamagedStack, unbreakableStack
        };
        Arrays.sort(stacks, FuzzySearch.COMPARATOR);
        assertThat(stacks).containsExactly(damagedStack, unbreakableStack, undamagedStack);
    }

    @Nested
    class Bounds {
        AEItemKey stack = AEItemKey.of(Items.DIAMOND_SWORD);
        AEItemKey damagedStack;

        {
            var tempStack = stack.toStack();
            tempStack.setDamageValue(tempStack.getMaxDamage());
            damagedStack = AEItemKey.of(tempStack);
        }

        @Test
        void testIgnoreAll() {
            DamageBounds bounds = new DamageBounds(stack, FuzzyMode.IGNORE_ALL);
            assertEquals(stack.getItem().getMaxDamage(), bounds.lower.itemDamage());
            assertEquals(-1, bounds.upper.itemDamage());
        }

        /**
         * Unbreakable items are still considered at their current damage value.
         */
        @Test
        void test99PercentDurabilityWithUnbreakable() {
            ItemStack unbreakableStack = damagedStack.toStack();
            unbreakableStack.getOrCreateTag().putBoolean("Unbreakable", true);
            assertFalse(unbreakableStack.isDamageableItem());

            DamageBounds bounds = new DamageBounds(AEItemKey.of(unbreakableStack), FuzzyMode.PERCENT_99);
            assertEquals(stack.getItem().getMaxDamage(), bounds.lower.itemDamage());
            assertEquals(0, bounds.upper.itemDamage());
        }

        /**
         * PERCENT_99 with an undamaged item should select only undamaged items, which translates to a damage range of
         * [0, -1).
         */
        @Test
        void test99PercentDurabilityWithUndamagedItem() {
            DamageBounds bounds = new DamageBounds(stack, FuzzyMode.PERCENT_99);
            assertEquals(0, bounds.lower.itemDamage());
            assertEquals(-1, bounds.upper.itemDamage());
        }

        /**
         * PERCENT_99 with a damaged item should select only damaged items, which translates to a damage range of
         * [maxDmg, 0).
         */
        @Test
        void test99PercentDurabilityWithDamagedItem() {
            DamageBounds bounds = new DamageBounds(damagedStack, FuzzyMode.PERCENT_99);
            assertEquals(stack.getItem().getMaxDamage(), bounds.lower.itemDamage());
            assertEquals(0, bounds.upper.itemDamage());
        }

        /**
         * PERCENT_75 with an undamaged item should select items that have 75% or more durability, which should
         * translate to a damage range of [0.25*maxDmg, -1).
         */
        @Test
        void test75PercentWithUndamagedItem() {
            DamageBounds bounds = new DamageBounds(stack, FuzzyMode.PERCENT_75);
            assertEquals((int) (0.25 * stack.getItem().getMaxDamage()), bounds.lower.itemDamage());
            assertEquals(-1, bounds.upper.itemDamage());
        }

        /**
         * PERCENT_75 with a damaged item should select items that have less than 75% durability, which should translate
         * to a damage range of [maxDmg, 0.25*maxDmg).
         */
        @Test
        void test75PercentWithDamagedItem() {
            DamageBounds bounds = new DamageBounds(damagedStack, FuzzyMode.PERCENT_75);
            assertEquals(stack.getItem().getMaxDamage(), bounds.lower.itemDamage());
            assertEquals((int) (0.25 * stack.getItem().getMaxDamage()), bounds.upper.itemDamage());
        }

        /**
         * PERCENT_50 with an undamaged item should select items that have 50% or more durability, which should
         * translate to a damage range of [0.50*maxDmg, -1).
         */
        @Test
        void test50PercentWithUndamagedItem() {
            DamageBounds bounds = new DamageBounds(stack, FuzzyMode.PERCENT_50);
            assertEquals((int) (0.50 * stack.getItem().getMaxDamage()), bounds.lower.itemDamage());
            assertEquals(-1, bounds.upper.itemDamage());
        }

        /**
         * PERCENT_50 with a damaged item should select items that have less than 50% durability, which should translate
         * to a damage range of [maxDmg, 0.50*maxDmg).
         */
        @Test
        void test50PercentWithDamagedItem() {
            DamageBounds bounds = new DamageBounds(damagedStack, FuzzyMode.PERCENT_50);
            assertEquals(stack.getItem().getMaxDamage(), bounds.lower.itemDamage());
            assertEquals((int) (0.50 * stack.getItem().getMaxDamage()), bounds.upper.itemDamage());
        }

        /**
         * PERCENT_25 with an undamaged item should select items that have 25% or more durability, which should
         * translate to a damage range of [0.75*maxDmg, -1).
         */
        @Test
        void test25PercentWithUndamagedItem() {
            DamageBounds bounds = new DamageBounds(stack, FuzzyMode.PERCENT_25);
            assertEquals((int) (0.75 * stack.getItem().getMaxDamage()), bounds.lower.itemDamage());
            assertEquals(-1, bounds.upper.itemDamage());
        }

        /**
         * PERCENT_25 with a damaged item should select items that have less than 25% durability, which should translate
         * to a damage range of [maxDmg, 0.75*maxDmg).
         */
        @Test
        void test25PercentWithDamagedItem() {
            DamageBounds bounds = new DamageBounds(damagedStack, FuzzyMode.PERCENT_25);
            assertEquals(stack.getItem().getMaxDamage(), bounds.lower.itemDamage());
            assertEquals((int) (0.75 * stack.getItem().getMaxDamage()), bounds.upper.itemDamage());
        }

    }

    private static class DamageBounds {
        final FuzzySearch.FuzzyBound lower;
        final FuzzySearch.FuzzyBound upper;

        public DamageBounds(AEKey what, FuzzyMode mode) {
            lower = FuzzySearch.makeLowerBound(what, mode);
            upper = FuzzySearch.makeUpperBound(what, mode);

            // This may be counter intuitive, but the map is sorted in descending order of item damage
            assertThat(lower.itemDamage()).isGreaterThan(upper.itemDamage());
        }
    }

}
