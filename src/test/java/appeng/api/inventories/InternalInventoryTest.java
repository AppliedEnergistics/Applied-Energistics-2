package appeng.api.inventories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterators;
import com.google.common.collect.testing.IteratorTester;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoSettings;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import appeng.util.BootstrapMinecraft;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

@BootstrapMinecraft
class InternalInventoryTest {

    @Nested
    class SubInventory {
        @Test
        void rangeCheckOnCreation() {
            var inv = new AppEngInternalInventory(1);
            assertThrows(IllegalArgumentException.class, () -> inv.getSubInventory(0, 2));
            assertThrows(IllegalArgumentException.class, () -> inv.getSubInventory(-1, 1));
            assertThrows(IllegalArgumentException.class, () -> inv.getSubInventory(1, 2));
            assertThrows(IllegalArgumentException.class, () -> inv.getSubInventory(1, 0));
        }

        // We place these stacks such that they're the first and last in the sub-inventory
        ItemStack firstItem = new ItemStack(Items.STICK);
        ItemStack lastItem = new ItemStack(Items.FLINT);
        InternalInventory inv;
        InternalInventory subInv;

        @BeforeEach
        void setup() {
            inv = new AppEngInternalInventory(10);
            inv.setItemDirect(0, new ItemStack(Items.BEDROCK));
            inv.setItemDirect(9, new ItemStack(Items.BEDROCK));
            inv.setItemDirect(1, firstItem);
            inv.setItemDirect(8, lastItem);
            subInv = inv.getSubInventory(1, 9);
        }

        @Test
        void testEmptySubInventory() {
            assertEquals(0, inv.getSubInventory(0, 0).size());
        }

        @Test
        void testSize() {
            assertEquals(8, subInv.size());
        }

        @Test
        void testIterator() {
            assertThat(subInv).containsExactly(firstItem, lastItem);
        }

        @Test
        void testGetStackInSlot() {
            assertSame(firstItem, subInv.getStackInSlot(0));
            assertSame(lastItem, subInv.getStackInSlot(7));
        }

        @Test
        void testGetStackInSlotRangeCheck() {
            assertThrows(IllegalArgumentException.class, () -> subInv.getStackInSlot(-1));
            assertThrows(IllegalArgumentException.class, () -> subInv.getStackInSlot(8));
        }

        @Test
        void testSetItemDirect() {
            subInv.setItemDirect(0, lastItem);
            subInv.setItemDirect(7, firstItem);
            assertSame(lastItem, inv.getStackInSlot(1));
            assertSame(firstItem, inv.getStackInSlot(8));
        }

        @Test
        void testSetItemDirectRangeCheck() {
            assertThrows(IllegalArgumentException.class, () -> subInv.setItemDirect(-1, firstItem));
            assertThrows(IllegalArgumentException.class, () -> subInv.setItemDirect(8, firstItem));
        }

        @Test
        void testInsertItem() {
            assertEquals(ItemStack.EMPTY, subInv.insertItem(0, firstItem.copy(), false));
            assertEquals(2, inv.getStackInSlot(1).getCount());
        }

        @Test
        void testInsertItemRangeCheck() {
            assertThrows(IllegalArgumentException.class, () -> subInv.insertItem(-1, firstItem, false));
            assertThrows(IllegalArgumentException.class, () -> subInv.insertItem(8, firstItem, false));
        }

        @Test
        void testExtractItem() {
            assertSame(firstItem, subInv.extractItem(0, 1, false));
            assertEquals(ItemStack.EMPTY, inv.getStackInSlot(1));
        }

        @Test
        void testExtractItemRangeCheck() {
            assertThrows(IllegalArgumentException.class, () -> subInv.extractItem(-1, 1, false));
            assertThrows(IllegalArgumentException.class, () -> subInv.extractItem(8, 1, false));
        }

        /**
         * Sub inventories acquired via sub-inventories should directly forward to the delegate rather than creating
         * pointless deep chains of proxy objects.
         */
        @Test
        void testSubInventoryOfSubInventoryDoesntChain() {
            var subSubInv = subInv.getSubInventory(7, 8);
            assertThat(subSubInv)
                    .usingFieldByFieldElementComparator()
                    .isEqualTo(inv.getSubInventory(8, 9));
        }

        /**
         * Slot inventories acquired via sub-inventories should directly forward to the delegate rather than creating
         * pointless deep chains of proxy objects.
         */
        @Test
        void testSlotInventoryOfSubInventoryDoesntChain() {
            var slotInv = subInv.getSlotInv(7);
            assertThat(slotInv)
                    .usingFieldByFieldElementComparator()
                    .isEqualTo(inv.getSlotInv(8));
        }
    }

    @Nested
    class SlotInventory {
        @Test
        void rangeCheckOnCreation() {
            var inv = new AppEngInternalInventory(1);
            assertThrows(IllegalArgumentException.class, () -> inv.getSlotInv(-1));
            assertThrows(IllegalArgumentException.class, () -> inv.getSlotInv(2));
        }

        ItemStack item = new ItemStack(Items.STICK);
        InternalInventory inv;
        InternalInventory slotInv;

        @BeforeEach
        void setup() {
            inv = new AppEngInternalInventory(3);
            inv.setItemDirect(1, item);
            slotInv = inv.getSlotInv(1);
        }

        @Test
        void testSize() {
            assertEquals(1, slotInv.size());
        }

        @Test
        void testSubInventoryDoesntChain() {
            var subSubInv = slotInv.getSubInventory(0, 1);
            assertThat(subSubInv)
                    .usingFieldByFieldElementComparator()
                    .isEqualTo(slotInv);
        }

        @Test
        void testSlotInventoryDoesntChain() {
            var subSubInv = slotInv.getSlotInv(0);
            assertThat(subSubInv)
                    .usingFieldByFieldElementComparator()
                    .isEqualTo(slotInv);
        }
    }

    @Nested
    class RedstoneSignal {
        @Test
        void testEmpty() {
            var inv = new AppEngInternalInventory(1);
            assertEquals(0, inv.getRedstoneSignal());
        }

        @Test
        void testNonEmptyInventoryIsAtLeastSignalStrengthOne() {
            var inv = new AppEngInternalInventory(1);
            inv.setItemDirect(0, new ItemStack(Items.STICK));
            assertEquals(1, inv.getRedstoneSignal());
        }

        @Test
        void testFull() {
            var inv = new AppEngInternalInventory(1);
            inv.setItemDirect(0, new ItemStack(Items.STICK, 64));
            assertEquals(15, inv.getRedstoneSignal());
        }

        @Test
        void testFullConsidersMaxStackSize() {
            var inv = new AppEngInternalInventory(1);
            inv.setItemDirect(0, new ItemStack(Items.ENDER_PEARL, 16));
            assertEquals(15, inv.getRedstoneSignal());
        }

        @Test
        void testFullAveragesAcrossSlots() {
            var inv = new AppEngInternalInventory(2);
            inv.setItemDirect(0, new ItemStack(Items.ENDER_PEARL, 8));
            inv.setItemDirect(1, new ItemStack(Items.STICK, 64));
            assertEquals((7 + 15) / 2, inv.getRedstoneSignal());
        }
    }

    /**
     * The iterable's primary point is that they skip empty slots.
     */
    @Nested
    class IterableTests {
        @Test
        void testNoSlotInventory() {
            assertThat(InternalInventory.empty()).containsOnly();
        }

        @Test
        void testEmptyInventory() {
            assertThat(new AppEngInternalInventory(64)).containsOnly();
        }

        /**
         * Checks that the iterator correctly deals with an inventory that has two slots populated, with different
         * amounts of empty space before, between and after the items.
         */
        @ParameterizedTest
        @CsvSource({
                "0,0,0",
                "1,0,0",
                "1,1,0",
                "1,1,1",
                "0,1,1",
                "0,0,1",
                "2,0,0",
                "2,2,0",
                "2,2,2",
                "0,2,2",
                "0,0,2",
        })
        void testWithTwoItems(int spaceBefore, int spaceBetween, int spaceAfter) {
            var boat = new ItemStack(Items.OAK_BOAT);
            var planks = new ItemStack(Items.ACACIA_PLANKS, 64);

            var inv = new AppEngInternalInventory(spaceBefore + spaceBetween + spaceAfter + 2);
            inv.setItemDirect(spaceBefore, boat);
            inv.setItemDirect(spaceBefore + 1 + spaceBetween, planks);

            var tester = new IteratorTester<>(
                    5,
                    Collections.emptyList(),
                    List.of(boat, planks),
                    IteratorTester.KnownOrder.KNOWN_ORDER) {

                @Override
                protected Iterator<ItemStack> newTargetIterator() {
                    return inv.iterator();
                }
            };
            tester.test();

            assertThat(inv).containsExactly(boat, planks);
        }
    }

    static abstract class AddItems {
        final AppEngInternalInventory inv;

        // Create an item that is stackable with something in this inventory
        final ItemStack stackableItem(int count) {
            return new ItemStack(Items.ENDER_PEARL, count);
        }

        // Create an item that is not stackable with something in this inventory
        final ItemStack nonStackableItem(int count) {
            var item = new ItemStack(Items.ENDER_PEARL, count);
            item.getOrCreateTag().putInt("xyz", 123);
            return item;
        }

        public AddItems(int size) {
            inv = new AppEngInternalInventory(size);
            // Slot 0 is empty.
            // Slot 1. Space left=0, because max stack size is 10 for this slot
            inv.setMaxStackSize(1, 10);
            inv.setItemDirect(1, stackableItem(10));
            // Slot 2. Non-Stackable due to NBT
            inv.setItemDirect(2, nonStackableItem(1));
            // Slot 3. Space left=1
            inv.setItemDirect(3, stackableItem(15));
            // Slot 4. Space left=0
            inv.setItemDirect(4, stackableItem(16));
            // Slot 6. Space left=2
            inv.setItemDirect(6, stackableItem(14));
        }

        @Test
        void testDoesNotAddEmptyItems() {
            var item = new ItemStack(Items.STICK, 0);
            assertEquals(ItemStack.EMPTY, inv.addItems(item));
            assertSame(ItemStack.EMPTY, inv.getStackInSlot(0));
        }

        // Tests that an item that already exceeds max stack size is split up even across empty slots
        // This test is equivalent between small/large inventories since it fills up anything up to the last
        // stackable slot
        @Test
        void testAddStackableItemAboveMaxStackSize() {
            assertSame(ItemStack.EMPTY, inv.simulateAdd(stackableItem(64)));
            assertSame(ItemStack.EMPTY, inv.addItems(stackableItem(64)));
            assertThat(reportAllSlots(inv))
                    .containsExactly(
                            "16 ender_pearl",
                            "10 ender_pearl",
                            "1 ender_pearl [has NBT]",
                            "16 ender_pearl",
                            "16 ender_pearl",
                            "16 ender_pearl",
                            "16 ender_pearl",
                            "16 ender_pearl",
                            "13 ender_pearl");
        }

        @Test
        void testFillUpInventoryExactly() {
            int emptySlots = inv.size() - Iterators.size(inv.iterator());
            // This is the amount of free space for ender pearls in the slots this test-class pre-populates (3)
            // plus in empty slots.
            int freeSpace = emptySlots * 16 + 3;

            assertSame(ItemStack.EMPTY, inv.simulateAdd(stackableItem(freeSpace)));
            assertSame(ItemStack.EMPTY, inv.addItems(stackableItem(freeSpace)));
            assertEquals(inv.size(), Iterators.size(inv.iterator()));
        }

        // Tests that the overflow is returned correctly
        @Test
        void testAddMoreItemsThanThereIsSpace() {
            int emptySlots = inv.size() - Iterators.size(inv.iterator());
            // This is the amount of free space for ender pearls in the slots this test-class pre-populates (3)
            // plus in empty slots.
            int freeSpace = emptySlots * 16 + 3;

            var stackable = stackableItem(freeSpace + 123);
            var overflow = inv.simulateAdd(stackable);
            // Stackable should not have been modified
            assertEquals(freeSpace + 123, stackable.getCount());
            assertFalse(overflow.isEmpty());
            assertEquals(123, overflow.getCount());

            overflow = inv.addItems(stackable);
            assertFalse(overflow.isEmpty());
            assertEquals(123, overflow.getCount());
        }
    }

    @Nested
    class AddItemsSmallInventory extends AddItems {
        public AddItemsSmallInventory() {
            // A player inventory should be considered a small inventory
            super(Inventory.INVENTORY_SIZE);
        }

        // For small inventories, we expect addItems to try and stack it onto existing slots
        @Test
        void testAddStackableItem() {
            assertSame(ItemStack.EMPTY, inv.simulateAdd(stackableItem(16)));
            assertSame(ItemStack.EMPTY, inv.addItems(stackableItem(16)));
            assertThat(reportAllSlots(inv))
                    .containsExactly(
                            // This was the remainder after stacking into everything else
                            "13 ender_pearl",
                            // Can't fill this one up past 10 due to a slot-count-limit
                            "10 ender_pearl",
                            // Can't fill because non-stackable (due to NBT)
                            "1 ender_pearl [has NBT]",
                            // Filled this from 15->16 due to max stack size
                            "16 ender_pearl",
                            // This slot was already at max stack size
                            "16 ender_pearl",
                            "1 air",
                            // Filled this from 14->16 due to max stack size
                            "16 ender_pearl");
        }
    }

    @Nested
    class AddItemsLargeInventory extends AddItems {
        public AddItemsLargeInventory() {
            // 54 is the double chest size, so one item beyond it should go to the large code path
            super(55);
        }

        // Unlike with small inventories, this will just start filling up from slot 0 upwards
        @Test
        void testAddStackableItem() {
            assertSame(ItemStack.EMPTY, inv.simulateAdd(stackableItem(16)));
            assertSame(ItemStack.EMPTY, inv.addItems(stackableItem(16)));
            assertThat(reportAllSlots(inv))
                    .containsExactly(
                            // This slot was empty
                            "16 ender_pearl",
                            "10 ender_pearl",
                            "1 ender_pearl [has NBT]",
                            "15 ender_pearl",
                            "16 ender_pearl",
                            "1 air",
                            "14 ender_pearl");
        }

    }

    @Nested
    class RemoveItems {

        AppEngInternalInventory inv = new AppEngInternalInventory(64);

        ItemStack item1;
        ItemStack item2;
        ItemStack item3;
        ItemStack item4;

        public RemoveItems() {
            item1 = new ItemStack(Items.STICK);
            // Same item, but with NBT to check that removeItem honors NBT differences
            item2 = new ItemStack(Items.STICK);
            item2.getOrCreateTag().putInt("x", 1);
            item3 = new ItemStack(Items.DIAMOND_SWORD);
            item4 = new ItemStack(Items.DIAMOND_SWORD);
            item4.setDamageValue(1);

            inv.setItemDirect(1, copyAmount(item1, 10));
            inv.setItemDirect(3, copyAmount(item2, 10));
            inv.setItemDirect(5, copyAmount(item3, 10));
            inv.setItemDirect(7, copyAmount(item4, 1));

            // Just repeat the items again
            inv.setItemDirect(10, copyAmount(item1, 5));
            inv.setItemDirect(11, copyAmount(item2, 5));
            inv.setItemDirect(12, copyAmount(item3, 5));
            inv.setItemDirect(13, copyAmount(item4, 1));
        }

        private static ItemStack copyAmount(ItemStack stack, int count) {
            var result = stack.copy();
            result.setCount(count);
            return result;
        }

        @Test
        void testWithEmptyInventory() {
            assertSame(ItemStack.EMPTY, InternalInventory.empty().removeItems(1, ItemStack.EMPTY, null));
        }

        @Nested
        class NoFilters {
            @Test
            void testTakeAll() {
                assertEquals("15 stick", inv.removeItems(15, ItemStack.EMPTY, null).toString());
                assertThat(reportFilledSlots(inv)).containsOnly(
                        "10 stick [has NBT]",
                        "10 diamond_sword [has NBT]",
                        "1 diamond_sword [has NBT]",
                        "5 stick [has NBT]",
                        "5 diamond_sword [has NBT]",
                        "1 diamond_sword [has NBT]");

                assertEquals("15 stick", inv.removeItems(15, ItemStack.EMPTY, null).toString());
                assertThat(reportFilledSlots(inv)).containsOnly(
                        "10 diamond_sword [has NBT]",
                        "1 diamond_sword [has NBT]",
                        "5 diamond_sword [has NBT]",
                        "1 diamond_sword [has NBT]");

                // Note how it'll extract only 1 sword of each slot per-iteration because
                // due to IItemHandler#extractItem being the basis for our extractItem,
                // it'll adhere to Vanilla stack size limits.
                assertEquals("2 diamond_sword", inv.removeItems(15, ItemStack.EMPTY, null).toString());
                assertThat(reportFilledSlots(inv)).containsOnly(
                        "9 diamond_sword [has NBT]",
                        "1 diamond_sword [has NBT]",
                        "4 diamond_sword [has NBT]",
                        "1 diamond_sword [has NBT]");
                // Extract the rest of it
                for (var i = 0; i < 4; i++) {
                    assertEquals("2 diamond_sword", inv.removeItems(15, ItemStack.EMPTY, null).toString());
                }
                for (var i = 0; i < 5; i++) {
                    assertEquals("1 diamond_sword", inv.removeItems(15, ItemStack.EMPTY, null).toString());
                }
                assertThat(reportFilledSlots(inv)).containsOnly(
                        "1 diamond_sword [has NBT]",
                        "1 diamond_sword [has NBT]");

                // Now extract the damaged sword
                var damagedSwords = inv.removeItems(15, ItemStack.EMPTY, null);
                assertEquals("2 diamond_sword", damagedSwords.toString());
                assertEquals(1, damagedSwords.getDamageValue());
                assertThat(reportFilledSlots(inv)).containsOnly();
            }

            @Test
            void testTakeLessThanAvailable() {
                assertEquals("14 stick", inv.removeItems(14, ItemStack.EMPTY, null).toString());
                assertThat(reportFilledSlots(inv)).containsOnly(
                        "10 stick [has NBT]",
                        "10 diamond_sword [has NBT]",
                        "1 diamond_sword [has NBT]",
                        // This is the remainder of the 15 sticks previously in the inventory
                        "1 stick",
                        "5 stick [has NBT]",
                        "5 diamond_sword [has NBT]",
                        "1 diamond_sword [has NBT]");
            }

            @Test
            void testTakeMoreThanAvailable() {
                assertEquals("15 stick", inv.removeItems(30, ItemStack.EMPTY, null).toString());

                assertThat(inv).noneMatch(s -> ItemStack.isSameItemSameTags(s, item1));
            }

            // When actual extraction for a slot is denied, the extraction does not lock onto the item type
            @Test
            void testReadOnlySlotsAreSkipped() {
                inv.setFilter(new IAEItemFilter() {
                    @Override
                    public boolean allowExtract(InternalInventory inv, int slot, int amount) {
                        return slot != 1;
                    }
                });
                var extracted = describeStack(inv.removeItems(15, ItemStack.EMPTY, null));
                assertEquals("15 stick [has NBT]", extracted);
            }
        }

        @Nested
        @MockitoSettings
        class DestinationFilter {

            @Test
            void testCallOrder() {
                var calls = new ArrayList<ItemStack>();

                // Only accept item 2
                assertEquals("15 stick", inv.removeItems(15, ItemStack.EMPTY, is -> {
                    calls.add(is.copy());
                    return ItemStack.isSameItemSameTags(is, item2);
                }).toString());

                // Up until the filter returns true, it will be called for all items,
                // after it returns true, it should only be called for items stackable with the first it accepted.
                assertThat(calls)
                        .extracting(InternalInventoryTest::describeStack)
                        .containsExactly(
                                "10 stick", "10 stick [has NBT]", "5 stick [has NBT]");
            }

            // Test that removeItems performs a simulated extract and passes the result of that to the filter
            // This can be observed by extracting swords, because the simulated extract will only return 1
            // due to the max stack size of swords.
            @Test
            void testCalledWithSimulatedExtractionResult() {
                var calls = new ArrayList<ItemStack>();
                inv.removeItems(2, ItemStack.EMPTY, is -> {
                    if (ItemStack.isSameItemSameTags(is, item3)) {
                        calls.add(is);
                        return true;
                    }
                    return false;
                });

                assertThat(calls)
                        .extracting(InternalInventoryTest::describeStack)
                        .containsOnly(
                                // It's called twice because there are two slots.
                                "1 diamond_sword [has NBT]",
                                "1 diamond_sword [has NBT]");
            }

            @Test
            void testNotCalledWhenInventoryDisallowsExtraction() {
                inv.setFilter(new IAEItemFilter() {
                    @Override
                    public boolean allowExtract(InternalInventory inv, int slot, int amount) {
                        return false;
                    }
                });

                inv.removeItems(1, ItemStack.EMPTY, is -> {
                    Assertions.fail("Should not be called for read-only invs");
                    return true;
                });
            }
        }

    }

    private static List<String> reportFilledSlots(InternalInventory inv) {
        return reportSlots(inv, false);
    }

    private static List<String> reportAllSlots(InternalInventory inv) {
        return reportSlots(inv, true);
    }

    private static List<String> reportSlots(InternalInventory inv, boolean includingEmpty) {
        for (int i = inv.size(); i > 0; i--) {
            if (!inv.getStackInSlot(i - 1).isEmpty()) {
                List<String> result = new ArrayList<>();
                for (int j = 0; j < i; j++) {
                    if (!includingEmpty && inv.getStackInSlot(j).isEmpty()) {
                        continue;
                    }

                    result.add(describeStack(inv.getStackInSlot(j)));
                }
                return result;
            }
        }
        return Collections.emptyList();
    }

    private static String describeStack(ItemStack stack) {
        if (stack.hasTag()) {
            return stack + " [has NBT]";
        } else {
            return stack.toString();
        }
    }

}
