package appeng.util.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;

public class ItemListTest {

    ItemList itemList = new ItemList();

    /**
     * add should merge item stacks by adding stored/requestable counts, and setting craftable if it wasn't set before.
     */
    @Test
    public void testAddMergesAllStackProperties() {
        itemList.add(diamondSword(100, 1, 0, false));
        assertPreciseStackProperties(diamondSwordFilter(100), 1, 0, false);

        itemList.add(diamondSword(100, 0, 1, false));
        assertPreciseStackProperties(diamondSwordFilter(100), 1, 1, false);

        itemList.add(diamondSword(100, 0, 0, true));
        assertPreciseStackProperties(diamondSwordFilter(100), 1, 1, true);
    }

    /**
     * addStorage only considers {@link IAEItemStack#getStackSize()} and ignores other properties when merging stacks,
     * but inherits all properties when it's adding a new item.
     */
    @Test
    public void testAddStorageForNewItem() {
        // TODO: This might actually be incorrect, given how addrequestable et al behave
        itemList.addStorage(diamondSword(100, 1, 1, true));
        assertPreciseStackProperties(diamondSwordFilter(100), 1, 1, true);
    }

    @Test
    public void testAddStorageForExistingItem() {
        itemList.addStorage(diamondSword(100, 1, 0, false));
        itemList.addStorage(diamondSword(100, 1, 2, true));
        assertPreciseStackProperties(diamondSwordFilter(100), 2, 0, false);
    }

    /**
     * addRequestable only considers {@link IAEItemStack#getCountRequestable()} and sets the stored amount to 0 and
     * craftable to false when adding an item.
     */
    @Test
    public void testAddRequestableForNewItem() {
        itemList.addRequestable(diamondSword(100, 1, 2, true));
        assertPreciseStackProperties(diamondSwordFilter(100), 0, 2, false);
    }

    /**
     * addRequestable only considers {@link IAEItemStack#getCountRequestable()} when merging into an existing item.
     */
    @Test
    public void testAddRequestableForExistingItem() {
        itemList.addRequestable(diamondSword(100, 0, 1, false));
        itemList.addRequestable(diamondSword(100, 2, 1, true));
        assertPreciseStackProperties(diamondSwordFilter(100), 0, 2, false);
    }

    /**
     * addCraftable only considers {@link IAEItemStack#isCraftable()} and sets the stored and requestable amounts to 0
     * when adding an item.
     */
    @Test
    public void testAddCraftingForNewItem() {
        itemList.addCrafting(diamondSword(100, 1, 2, true));
        // TODO: I think it is unintended that the requestable amount is used
        assertPreciseStackProperties(diamondSwordFilter(100), 0, 2, true);
    }

    /**
     * addRequestable only considers {@link IAEItemStack#getCountRequestable()} when merging into an existing item.
     */
    @Test
    public void testAddCraftingForExistingItem() {
        itemList.addCrafting(diamondSword(100, 0, 0, false));
        itemList.addCrafting(diamondSword(100, 1, 2, true));
        assertPreciseStackProperties(diamondSwordFilter(100), 0, 0, true);
    }

    /**
     * an empty craftable stack still creates an entry in the list
     */
    @Test
    public void testAddEmptyStackThatIsCraftable() {
        itemList.add(diamondSword(100, 0, 0, true));
        assertPreciseStackProperties(diamondSwordFilter(100), 0, 0, true);
    }

    /**
     * check that craftable isn't accidentally reset to false when merging stacks
     */
    @Test
    public void testAddDoesNotResetCraftableBackToFalse() {
        itemList.add(diamondSword(100, 0, 0, true));
        itemList.add(diamondSword(100, 1, 0, false));
        assertPreciseStackProperties(diamondSwordFilter(100), 1, 0, true);
    }

    /**
     * stacks for the same item, but different damage values should not be merged
     */
    @Test
    public void testAddDoesNotMergeAcrossDamageValues() {
        itemList.add(diamondSword(100, 1, 0, false));
        itemList.add(diamondSword(99, 1, 0, false));
        assertPreciseStackProperties(diamondSwordFilter(100), 1, 0, false);
        assertPreciseStackProperties(diamondSwordFilter(99), 1, 0, false);
    }

    private void assertPreciseStackProperties(IAEItemStack stack, long stored, long requestable, boolean craftable) {
        IAEItemStack storedStack = itemList.findPrecise(stack);
        assertEquals(stored, storedStack.getStackSize(), "stored amount");
        assertEquals(requestable, storedStack.getCountRequestable(), "requestable amount");
        assertEquals(craftable, storedStack.isCraftable(), "craftable");
    }

    /**
     * Even if the stack has no stored or requestable amounts, it should be returned by the item list if it is
     * craftable.
     */
    @Test
    public void testSizeAndIterateForEmptyButCraftableStack() {
        itemList.add(diamondSword(100, 0, 0, true));
        assertListContent(diamondSword(100, 0, 0, true));
    }

    /**
     * If the stack is not craftable, an empty stack is actually ignored.
     */
    @Test
    public void testEmptyStackIsIgnored() {
        itemList.add(diamondSword(100, 0, 0, false));
        assertListContent();
    }

    @Test
    public void testResetStatus() {
        itemList.add(diamondSword(100, 1, 0, false));
        itemList.add(nameTag(1, 0, false));
        assertEquals(2, itemList.size());

        itemList.resetStatus();

        assertListContent(); // The list should now be empty
    }

    /**
     * Tests that iteration across multiple items and variations of those items works.
     */
    @Test
    public void testIterateAcrossMultipleItems() {
        // Add damaged variants of the same item, including NBT variants
        AEItemStack sword1 = diamondSword(100, 1, 0, false);
        itemList.add(sword1);
        AEItemStack sword2 = diamondSword(50, 1, 0, false);
        itemList.add(sword2);
        AEItemStack sword3 = diamondSword(25, 1, 0, false);
        itemList.add(sword3);
        AEItemStack sword4 = diamondSword(100, "master sword", 1, 0, false);
        itemList.add(sword4);
        // And a non-damagable item with different NBT
        AEItemStack nameTag1 = nameTag(1, 0, false);
        itemList.add(nameTag1);
        AEItemStack nameTag2 = nameTag("bob", 1, 0, false);
        itemList.add(nameTag2);

        assertListContent(sword1, sword2, sword3, sword4, nameTag1, nameTag2);
    }

    /**
     * Regression test for broken iterators that mutated state in {@see Iterator#hasNext}. This was the case for both
     * the top-level and sub-iterator.
     */
    @Test
    public void testIteratorHasNextDoesNotSkipItems() {
        itemList.add(diamondSword(100, 1, 0, false));
        itemList.add(diamondSword(50, 1, 0, false));

        Iterator<IAEItemStack> it = itemList.iterator();
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
    }

    @Test
    public void testGetFirstItemForEmptyList() {
        assertNull(itemList.getFirstItem());
    }

    @Test
    public void testGetFirstItem() {
        AEItemStack itemStack = diamondSword(100, 1, 0, false);
        itemList.add(itemStack);
        // The order is no longer well defined w.r.t. the hashmap
        assertEquals(itemStack, itemList.getFirstItem());
    }

    @Nested
    class FindFuzzyDamageableItems {

        // Swords to cover all durability values
        AEItemStack swordAbove100 = diamondSword(101, 1, 0, false);
        AEItemStack[] swords = new AEItemStack[101];
        // Filters for inverting the filter as needed
        AEItemStack undamagedFilter = diamondSwordFilter(100);
        AEItemStack damagedFilter = diamondSwordFilter(0);

        @BeforeEach
        void addItems() {
            itemList.add(swordAbove100);
            for (int i = 0; i <= 100; i++) {
                swords[i] = diamondSword(i, 1, 0, false);
                assertEquals(i, getDurabilityPercent(swords[i]));
                itemList.add(swords[i]);
            }
        }

        @Test
        public void testIgnoreAllWithUndamagedFilter() {
            assertReturnedDurabilities(undamagedFilter, FuzzyMode.IGNORE_ALL, 0, 100, false);
        }

        @Test
        public void testIgnoreAllWithDamagedFilter() {
            assertReturnedDurabilities(damagedFilter, FuzzyMode.IGNORE_ALL, 0, 100, false);
        }

        @Test
        public void testPercent99WithUndamagedFilter() {
            assertReturnedDurabilities(undamagedFilter, FuzzyMode.PERCENT_99, 100, 100, false);
        }

        @Test
        public void testPercent99WithDamagedFilter() {
            assertReturnedDurabilities(damagedFilter, FuzzyMode.PERCENT_99, 0, 99, false);
        }

        @Test
        public void testPercent75WithUndamagedFilter() {
            assertReturnedDurabilities(undamagedFilter, FuzzyMode.PERCENT_75, 75, 100, false);
        }

        @Test
        public void testPercent75WithDamagedFilter() {
            assertReturnedDurabilities(damagedFilter, FuzzyMode.PERCENT_75, 0, 74, false);
        }

        @Test
        public void testPercent50WithUndamagedFilter() {
            assertReturnedDurabilities(undamagedFilter, FuzzyMode.PERCENT_50, 50, 100, false);
        }

        @Test
        public void testPercent50WithDamagedFilter() {
            assertReturnedDurabilities(damagedFilter, FuzzyMode.PERCENT_50, 0, 49, false);
        }

        @Test
        public void testPercent25WithUndamagedFilter() {
            assertReturnedDurabilities(undamagedFilter, FuzzyMode.PERCENT_25, 25, 100, false);
        }

        @Test
        public void testPercent25WithDamagedFilter() {
            assertReturnedDurabilities(damagedFilter, FuzzyMode.PERCENT_25, 0, 24, false);
        }

        private void assertReturnedDurabilities(IAEItemStack filter, FuzzyMode fuzzyMode, int minDurabilityInclusive,
                int maxDurabilityInclusive, boolean above100) {
            Collection<IAEItemStack> items = itemList.findFuzzy(filter, fuzzyMode);

            // Build a list of the durabilities that got returned
            List<Integer> durabilities = items.stream().map(this::getDurabilityPercent)
                    .sorted()
                    .collect(Collectors.toList());

            // Build a sorted list of the durabilities we expect
            List<Integer> expectedDurabilities = new ArrayList<>();
            for (int i = minDurabilityInclusive; i <= maxDurabilityInclusive; i++) {
                expectedDurabilities.add(getDurabilityPercent(swords[i]));
            }
            if (above100) {
                expectedDurabilities.add(getDurabilityPercent(swordAbove100));
            }
            expectedDurabilities.sort(Integer::compare);

            assertEquals(expectedDurabilities, durabilities);
        }

        private int getDurabilityPercent(IAEItemStack stack) {
            if (stack == swordAbove100) {
                return 101;
            }
            return (int) ((1.0f - (stack.getItemDamage() / (float) stack.getDefinition().getMaxDamage())) * 100);
        }
    }

    @Test
    void testFindFuzzyForNormalItems() {
        AEItemStack item1 = nameTag(null, 1, 0, false);
        itemList.add(item1);
        AEItemStack item2 = nameTag("name1", 1, 0, false);
        itemList.add(item2);
        AEItemStack item3 = nameTag("name2", 1, 0, false);
        itemList.add(item3);
        // Add another item to ensure this is not returned
        itemList.add(AEItemStack.fromItemStack(new ItemStack(Items.CRAFTING_TABLE)));

        for (FuzzyMode fuzzyMode : FuzzyMode.values()) {
            Collection<IAEItemStack> result = itemList.findFuzzy(nameTag(null, 0, 0, false), fuzzyMode);
            assertThat(result).containsOnly(item1, item2, item3);
        }
    }

    /**
     * Tests how ItemList behaves w.r.t. null arguments, given that {@link AEItemStack#fromItemStack(ItemStack)}
     * can return null for an empty stack, this sometimes leaks into method parameters.
     * As such, methods should behave as if an empty item stack was passed.
     */
    @Nested
    class NullArguments {
        @BeforeEach
        void addItem() {
            itemList.add(diamondSword(100, 1, 0, false));
        }

        @Test
        void testFindFuzzy() {
            assertThat(itemList.findFuzzy(null, FuzzyMode.PERCENT_99)).isEmpty();
        }

        @Test
        void testFindPrecise() {
            assertThat(itemList.findPrecise(null)).isNull();
        }

        @Test
        void testAdd() {
            assertThat(itemList.size()).isEqualTo(1);
            itemList.add(null);
            assertThat(itemList.size()).isEqualTo(1);
        }

        @Test
        void testAddStorage() {
            assertThat(itemList.size()).isEqualTo(1);
            itemList.addStorage(null);
            assertThat(itemList.size()).isEqualTo(1);
        }

        @Test
        void testAddRequestable() {
            assertThat(itemList.size()).isEqualTo(1);
            itemList.addRequestable(null);
            assertThat(itemList.size()).isEqualTo(1);
        }

        @Test
        void testAddCrafting() {
            assertThat(itemList.size()).isEqualTo(1);
            itemList.addCrafting(null);
            assertThat(itemList.size()).isEqualTo(1);
        }
    }

    private void assertListContent(AEItemStack... stacks) {
        assertEquals(stacks.length == 0, itemList.isEmpty(), "isEmpty");
        assertEquals(stacks.length, itemList.size());
        assertEquals(ImmutableSet.copyOf(stacks), ImmutableSet.copyOf(itemList));
    }

    private AEItemStack diamondSwordFilter(int durabilityPercent) {
        return diamondSword(durabilityPercent, 0, 0, false);
    }

    private AEItemStack diamondSword(int durabilityPercent, long stored, long requestable, boolean craftable) {
        return diamondSword(durabilityPercent, null, stored, requestable, craftable);
    }

    private AEItemStack diamondSword(int durabilityPercent, String customName, long stored, long requestable,
            boolean craftable) {
        ItemStack is = new ItemStack(Items.DIAMOND_SWORD);
        if (customName != null) {
            is.setDisplayName(new StringTextComponent(customName));
        }
        int damage = (int) ((100 - durabilityPercent) / 100.0f * is.getMaxDamage());
        is.setDamage(damage);
        AEItemStack ais = AEItemStack.fromItemStack(is);
        ais.setStackSize(stored);
        ais.setCountRequestable(requestable);
        ais.setCraftable(craftable);
        return ais;
    }

    // customName can be used to create items that differ in NBT
    private AEItemStack nameTag(long stored, long requestable, boolean craftable) {
        return nameTag(null, stored, requestable, craftable);
    }

    private AEItemStack nameTag(String customName, long stored, long requestable, boolean craftable) {
        ItemStack is = new ItemStack(Items.NAME_TAG);
        if (customName != null) {
            is.setDisplayName(new StringTextComponent(customName));
        }
        AEItemStack ais = AEItemStack.fromItemStack(is);
        ais.setStackSize(stored);
        ais.setCountRequestable(requestable);
        ais.setCraftable(craftable);
        return ais;
    }

}