package appeng.api.inventories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import appeng.core.definitions.AEItems;
import appeng.util.BootstrapMinecraft;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;

@BootstrapMinecraft
class FabricInternalInventoryTest {
    InternalInventoryHost host = Mockito.mock(InternalInventoryHost.class);

    AppEngInternalInventory inv = new AppEngInternalInventory(host, 3);

    Storage<ItemVariant> storage = inv.toStorage();

    ItemStack unstackable = AEItems.ITEM_CELL_64K.stack();
    ItemStack unstackableCopy = unstackable.copy();

    ItemStack stackable = new ItemStack(Items.OAK_PLANKS, 64);
    ItemStack stackableCopy = stackable.copy();

    public FabricInternalInventoryTest() {
        inv.setItemDirect(0, unstackable);
        inv.setItemDirect(2, stackable);
        Mockito.clearInvocations(host);
    }

    @Test
    void testAbortedTransactionWithoutChanges() {
        try (var tx = Transaction.openOuter()) {
            assertEquals(
                    new ResourceAmount<>(ItemVariant.of(unstackable), unstackable.getCount()),
                    StorageUtil.findExtractableContent(storage, tx));
        }
        assertUnchanged();
    }

    @Test
    void testComittedTransactionWithoutChanges() {
        try (var tx = Transaction.openOuter()) {
            assertEquals(
                    new ResourceAmount<>(ItemVariant.of(unstackable), unstackable.getCount()),
                    StorageUtil.findExtractableContent(storage, tx));
            tx.commit();
        }
        assertUnchanged();
    }

    @Test
    void testAbortedTransactionWithChangesToStackSize1() {
        try (var tx = Transaction.openOuter()) {
            assertEquals(1, storage.extract(ItemVariant.of(unstackable), 1, tx));
        }
        assertUnchanged();
    }

    @Test
    void testAbortedTransactionWithChangesToStackSize64() {
        try (var tx = Transaction.openOuter()) {
            assertEquals(1, storage.extract(ItemVariant.of(stackable), 1, tx));
        }
        assertUnchanged();
    }

    @Test
    void testComittedTransactionWithChangesToStackSize1() {
        try (var tx = Transaction.openOuter()) {
            assertEquals(1, storage.extract(ItemVariant.of(unstackable), 1, tx));
            tx.commit();
        }

        assertSame(ItemStack.EMPTY, inv.getStackInSlot(0));
        verify(host).onChangeInventory(inv, 0);
        assertStackableIsUnchanged();
    }

    @Test
    void testComittedTransactionWithChangesToStackSize64() {
        try (var tx = Transaction.openOuter()) {
            assertEquals(1, storage.extract(ItemVariant.of(stackable), 1, tx));
            tx.commit();
        }

        assertSame(stackable.getItem(), inv.getStackInSlot(2).getItem());
        assertEquals(63, inv.getStackInSlot(2).getCount());
        verify(host).onChangeInventory(inv, 2);
        assertUnstackableIsUnchanged();
    }

    private void assertUnchanged() {
        assertUnstackableIsUnchanged();
        assertSame(ItemStack.EMPTY, inv.getStackInSlot(1));
        assertStackableIsUnchanged();
        Mockito.verifyNoMoreInteractions(host);
    }

    private void assertStackableIsUnchanged() {
        assertSame(stackable, inv.getStackInSlot(2));
        assertTrue(ItemStack.matches(stackableCopy, stackable), "The item in the third slot was modified in-place");
        verify(host, never()).onChangeInventory(same(inv), eq(2));
    }

    private void assertUnstackableIsUnchanged() {
        assertSame(unstackable, inv.getStackInSlot(0));
        assertTrue(ItemStack.matches(unstackableCopy, unstackable), "The item in the first slot was modified in-place");
        verify(host, never()).onChangeInventory(same(inv), eq(0));
    }
}
