package appeng.helpers.iface;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Items;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.util.BootstrapMinecraft;
import appeng.util.ConfigInventory;

@BootstrapMinecraft
class GenericStackInvTest {
    public static final AEItemKey STICK_KEY = AEItemKey.of(Items.STICK);
    public static final GenericStack ONE_STICK = new GenericStack(STICK_KEY, 1);
    private final AtomicInteger changeNotifications = new AtomicInteger();

    private final GenericStackInv inv = new GenericStackInv(changeNotifications::incrementAndGet, 2);

    @Test
    void testIsEmptyOnEmptyInventory() {
        assertTrue(inv.isEmpty());
    }

    @Test
    void testIsEmptyOnNonEmptyInventory() {
        inv.setStack(0, ONE_STICK);
        assertFalse(inv.isEmpty());
    }

    /**
     * Tests that saving a larger inventory and loading it into a smaller one works and just loads the first few slots.
     */
    @Test
    void testSaveLargeAndLoadIntoSmallerInventory() {
        var large = ConfigInventory.configStacks(AEItemKey.filter(), 2, null);
        large.setStack(0, ONE_STICK);
        large.setStack(1, ONE_STICK);

        inv.readFromTag(large.writeToTag());

        assertEquals(ONE_STICK, inv.getStack(0));
        // Size didnt change...
        assertEquals(2, inv.size());
    }

    @Test
    void testLoadingFromEmptyTagClearsFilledSlots() {
        inv.setStack(0, ONE_STICK);
        inv.readFromTag(new ListTag());
        assertNull(inv.getStack(0));
    }

    @Test
    void testWritingAnEmptyInventoryProducesAnEmptyTag() {
        assertEquals(0, inv.writeToTag().size());
    }

    /**
     * We optimize empty inventories by not saving their child tag.
     */
    @Test
    void testWritingAnEmptyInventoryProducesNoChildTag() {
        var tag = new CompoundTag();
        inv.writeToChildTag(tag, "child");
        assertEquals(new CompoundTag(), tag);
    }

    /**
     * Writing to child tags works as expected and only produces the named child tag.
     */
    @Test
    void testWritingToChildTag() {
        var tag = new CompoundTag();
        inv.setStack(0, ONE_STICK);
        inv.writeToChildTag(tag, "child");
        assertThat(tag.getAllKeys()).containsOnly("child");
    }

    /**
     * Reading from child tags restores the inventory.
     */
    @Test
    void testReadingFromChildTag() {
        var tag = new CompoundTag();
        inv.setStack(0, ONE_STICK);
        inv.writeToChildTag(tag, "child");
        inv.clear();
        changeNotifications.set(0);
        inv.readFromChildTag(tag, "child");

        assertEquals(ONE_STICK, inv.getStack(0));
        assertEquals(1, changeNotifications.get());
    }

    /**
     * Change notifications shouldn't occur if readFromTag doesn't actually change anything
     */
    @Test
    void testReadFromTagOnlyNotifiesOnChanges() {
        var otherInv = new GenericStackInv(null, 2);
        otherInv.setStack(0, ONE_STICK);

        // Read once
        inv.readFromTag(otherInv.writeToTag());
        assertEquals(1, changeNotifications.get());

        // Read again
        inv.readFromTag(otherInv.writeToTag());
        assertEquals(1, changeNotifications.get());

        // Notification on clear
        inv.readFromTag(new ListTag());
        assertEquals(2, changeNotifications.get());
    }

    @Test
    void testClear() {
        inv.setStack(0, ONE_STICK);
        inv.setStack(1, ONE_STICK);
        changeNotifications.set(0);
        inv.clear();
        assertTrue(inv.isEmpty());

        // Should only call change notifications once
        assertEquals(1, changeNotifications.get());
    }

    @Test
    void testClearDoesntNotifyWhenNothingChanges() {
        inv.clear();
        assertEquals(0, changeNotifications.get());
    }

    /**
     * readFromChildTag should clear the inventory if the tag doesn't exist.
     */
    @Test
    void testReadingFromMissingChildTag() {
        inv.setStack(0, ONE_STICK);
        inv.readFromChildTag(new CompoundTag(), "child");
        assertNull(inv.getStack(0));
    }

    @Test
    void testGetKeyForEmptySlot() {
        assertNull(inv.getKey(0));
    }

    @Test
    void testGetAmountForEmptySlot() {
        assertEquals(0, inv.getAmount(0));
    }

    @Test
    void testGetKeyForFilledSlot() {
        inv.setStack(0, ONE_STICK);
        assertEquals(STICK_KEY, inv.getKey(0));
    }

    @Test
    void testGetAmountForFilledSlot() {
        inv.setStack(0, ONE_STICK);
        assertEquals(1, inv.getAmount(0));
    }

    @Nested
    class ChangeBatching {
        @Test
        void testChangeBatching() {
            inv.beginBatch();
            inv.setStack(0, ONE_STICK);
            inv.setStack(0, null);
            inv.setStack(0, ONE_STICK);
            inv.readFromTag(new ListTag());
            assertEquals(0, changeNotifications.get());
            inv.endBatch();
            assertEquals(1, changeNotifications.get());
        }

        @Test
        void testEndBatchDoesntNotifyIfNothingHappened() {
            inv.beginBatch();
            inv.endBatch();
            assertEquals(0, changeNotifications.get());
        }

        @Test
        void testBeginAndEndBatchMustBePaired() {
            inv.beginBatch();
            assertThrows(IllegalStateException.class, inv::beginBatch);
            inv.endBatch();
            assertThrows(IllegalStateException.class, inv::endBatch);
        }
    }

    @Nested
    class Insert {
        @Test
        void testInsertIntoEmptySlot() {
            assertEquals(1, inv.insert(0, STICK_KEY, 1, Actionable.SIMULATE));
            assertNull(inv.getStack(0));
            assertEquals(1, inv.insert(0, STICK_KEY, 1, Actionable.MODULATE));
            assertEquals(ONE_STICK, inv.getStack(0));
        }
    }

    @Nested
    class Extract {
        @Test
        void testPartialExtract() {
            inv.setStack(0, new GenericStack(STICK_KEY, 32));
            assertEquals(10, inv.extract(0, STICK_KEY, 10, Actionable.SIMULATE));
            assertEquals(new GenericStack(STICK_KEY, 32), inv.getStack(0));
            assertEquals(10, inv.extract(0, STICK_KEY, 10, Actionable.MODULATE));
            assertEquals(new GenericStack(STICK_KEY, 22), inv.getStack(0));
        }
    }
}
