package appeng.api.inventories;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;

import appeng.core.definitions.AEItems;

class InternalInventoryStorage extends SnapshotParticipant<List<ItemStack>> implements Storage<ItemVariant> {
    private final InternalInventory inventory;

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
    public Iterator<StorageView<ItemVariant>> iterator(TransactionContext transaction) {
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

                    // TODO FABRIC 117: DISGUSTING. Must update snapshot only for this slot.
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
    protected List<ItemStack> createSnapshot() {
        // TODO FABRIC 117: DISGUSTING.
        List<ItemStack> snapshot = new ArrayList<>(inventory.size());
        for (int i = 0; i < inventory.size(); i++) {
            snapshot.add(inventory.getStackInSlot(i).copy());
        }
        return snapshot;
    }

    @Override
    protected void readSnapshot(List<ItemStack> snapshot) {
        for (int i = 0; i < snapshot.size(); i++) {
            inventory.setItemDirect(i, snapshot.get(i));
        }
    }
}
