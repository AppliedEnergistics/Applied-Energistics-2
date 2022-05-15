package appeng.api.inventories;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;

import appeng.core.definitions.AEItems;

class InternalInventoryStorage extends SnapshotParticipant<InternalInventoryStorage.Snapshot>
        implements Storage<ItemVariant> {
    private final InternalInventory inventory;
    @Nullable
    private Snapshot lastReleasedSnapshot;

    public InternalInventoryStorage(InternalInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        var stack = resource.toStack((int) Math.min(Integer.MAX_VALUE, maxAmount));

        updateSnapshots(transaction);

        var overflow = inventory.addItems(stack);
        return maxAmount - overflow.getCount();
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        // Do not allow extraction of wrapped fluid stacks because they're an internal detail
        if (resource.getItem() == AEItems.WRAPPED_GENERIC_STACK.asItem()) {
            return 0;
        }

        updateSnapshots(transaction);

        var amt = (int) Math.min(Integer.MAX_VALUE, maxAmount);
        ItemStack extracted = inventory.removeItems(amt, resource.toStack(), null);

        return extracted.getCount();
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return new InventoryIterator();
    }

    private class InventoryIterator implements Iterator<StorageView<ItemVariant>> {
        private int currentSlot = -1;

        @Override
        public boolean hasNext() {
            return currentSlot + 1 < inventory.size();
        }

        @Override
        public StorageView<ItemVariant> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            currentSlot++;

            return new StorageView<>() {
                @Override
                public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                    StoragePreconditions.notBlankNotNegative(resource, maxAmount);

                    // Do not allow extraction of wrapped fluid stacks because they're an internal detail
                    if (resource.getItem() == AEItems.WRAPPED_GENERIC_STACK.asItem()) {
                        return 0;
                    }

                    updateSnapshots(transaction);

                    return inventory.extractItem(currentSlot, (int) Math.min(Integer.MAX_VALUE, maxAmount), false)
                            .getCount();
                }

                @Override
                public boolean isResourceBlank() {
                    return inventory.getStackInSlot(currentSlot).isEmpty();
                }

                @Override
                public ItemVariant getResource() {
                    return ItemVariant.of(inventory.getStackInSlot(currentSlot));
                }

                @Override
                public long getAmount() {
                    return inventory.getStackInSlot(currentSlot).getCount();
                }

                @Override
                public long getCapacity() {
                    return inventory.getSlotLimit(currentSlot);
                }
            };
        }
    }

    @Override
    protected Snapshot createSnapshot() {
        Snapshot snapshot;
        if (this.lastReleasedSnapshot != null && this.lastReleasedSnapshot.items.length == inventory.size()) {
            snapshot = this.lastReleasedSnapshot;
            this.lastReleasedSnapshot = null;
        } else {
            snapshot = new Snapshot();
        }

        for (int i = 0; i < inventory.size(); i++) {
            var stack = inventory.getStackInSlot(i);
            snapshot.items[i] = stack;
            snapshot.counts[i] = stack.getCount();
        }
        return snapshot;
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        var items = snapshot.items;
        var counts = snapshot.counts;
        for (int i = 0; i < items.length; i++) {
            var stack = items[i];
            // Restore the previous count as well, the inventory might mutate the stack count for extract/insert
            // We do not restore NBT since the Storage API does not give access to the original NBT and the inventory
            // doesn't mutate it itself
            if (stack.getCount() != counts[i]) {
                stack.setCount(counts[i]);
            }
            inventory.setItemDirect(i, stack);
        }
    }

    @Override
    protected void releaseSnapshot(Snapshot snapshot) {
        this.lastReleasedSnapshot = snapshot;
    }

    public class Snapshot {
        ItemStack[] items;
        int[] counts;

        public Snapshot() {
            this.items = new ItemStack[inventory.size()];
            this.counts = new int[inventory.size()];
        }
    }

    @Override
    protected void onFinalCommit() {
        // Diff the last snapshot against the inventory to collect change notifications
        Preconditions.checkState(lastReleasedSnapshot != null, "There should have been at least one snapshot");

        for (int i = 0; i < lastReleasedSnapshot.items.length; i++) {
            var current = inventory.getStackInSlot(i);
            if (current != lastReleasedSnapshot.items[i] || current.getCount() != lastReleasedSnapshot.counts[i]) {
                inventory.sendChangeNotification(i);
            }
        }
    }
}
