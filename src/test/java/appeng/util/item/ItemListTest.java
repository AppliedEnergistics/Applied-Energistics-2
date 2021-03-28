package appeng.util.item;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Iterator;

import com.google.common.collect.ImmutableList;
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
    class FindFuzzy {

        AEItemStack swordMinus1 = diamondSword(-1, 1, 0, false);
        AEItemStack sword25 = diamondSword(25, 1, 0, false);
        AEItemStack sword26 = diamondSword(26, 1, 0, false);
        AEItemStack sword50 = diamondSword(50, 1, 0, false);
        AEItemStack sword51 = diamondSword(51, 1, 0, false);
        AEItemStack sword75 = diamondSword(75, 1, 0, false);
        AEItemStack sword76 = diamondSword(76, 1, 0, false);
        AEItemStack sword99 = diamondSword(99, 1, 0, false);
        AEItemStack sword100 = diamondSword(100, 1, 0, false);
        AEItemStack sword101 = diamondSword(101, 1, 0, false);
        AEItemStack filter = diamondSwordFilter(100);

        @BeforeEach
        void addItems() {
            add(swordMinus1, sword25, sword26, sword50, sword51, sword75, sword76, sword99, sword100, sword101);
        }

        @Test
        public void testIgnoreAll() {
            Collection<IAEItemStack> found = itemList.findFuzzy(filter, FuzzyMode.IGNORE_ALL);
            assertIterableEquals(
                    // Note how the "broken" ones (-1%, 101% durability) are not returned
                    ImmutableList.of(sword100, sword99, sword76, sword75, sword51, sword50, sword26, sword25),
                    found);
        }

        @Test
        public void test99PercentOrLessDurability() {
            Collection<IAEItemStack> found = itemList.findFuzzy(filter, FuzzyMode.PERCENT_99);
            assertIterableEquals(
                    // Note how the "broken" one (-1%) is not returned
                    ImmutableList.of(sword99, sword76, sword75, sword51, sword50, sword26, sword25),
                    found);
        }

        @Test
        public void test75PercentOrLessDurability() {
            Collection<IAEItemStack> found = itemList.findFuzzy(filter, FuzzyMode.PERCENT_75);
            assertIterableEquals(
                    // Note how the "broken" one (-1%) is not returned
                    ImmutableList.of(sword75, sword51, sword50, sword26, sword25),
                    found);
        }

        @Test
        public void test50PercentOrLessDurability() {
            Collection<IAEItemStack> found = itemList.findFuzzy(filter, FuzzyMode.PERCENT_50);
            assertIterableEquals(
                    // Note how the "broken" one (-1%) is not returned
                    ImmutableList.of(sword50, sword26, sword25),
                    found);
        }

        @Test
        public void test25PercentOrLessDurability() {
            Collection<IAEItemStack> found = itemList.findFuzzy(filter, FuzzyMode.PERCENT_25);
            assertIterableEquals(
                    // Note how the "broken" one (-1%) is not returned
                    ImmutableList.of(sword25),
                    found);
        }
    }

    private void add(IAEItemStack... stacks) {
        for (IAEItemStack stack : stacks) {
            itemList.add(stack);
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