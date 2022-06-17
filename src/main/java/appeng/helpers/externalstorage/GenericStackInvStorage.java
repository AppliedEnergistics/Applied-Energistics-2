package appeng.helpers.externalstorage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.util.IVariantConversion;

/**
 * Adapts a {@link GenericStackInv} as {@link net.fabricmc.fabric.api.transfer.v1.storage.Storage} of the appropriate
 * type.
 */
public class GenericStackInvStorage<V extends TransferVariant<?>> implements Storage<V> {
    private final IVariantConversion<V> conversion;
    private final GenericInternalInventory inv;
    private final AEKeyType channel;
    private final List<View> storageViews;

    public GenericStackInvStorage(IVariantConversion<V> conversion, AEKeyType channel,
            GenericInternalInventory inv) {
        this.conversion = conversion;
        this.channel = channel;
        this.inv = inv;
        this.storageViews = new ArrayList<>(inv.size());
        for (int i = 0; i < inv.size(); i++) {
            this.storageViews.add(new View(i));
        }
    }

    @Override
    public long insert(V resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (!inv.canInsert()) {
            return 0;
        }
        long totalInserted = 0;

        // First iteration matches the resource, second iteration inserts into empty slots.
        for (var view : storageViews) {
            if (!view.isResourceBlank()) {
                totalInserted += view.insert(resource, maxAmount - totalInserted, transaction);
                if (totalInserted >= maxAmount) {
                    break;
                }
            }
        }

        for (var view : storageViews) {
            if (view.isResourceBlank()) {
                totalInserted += view.insert(resource, maxAmount - totalInserted, transaction);
                if (totalInserted >= maxAmount) {
                    break;
                }
            }
        }

        return totalInserted;
    }

    @Override
    public long extract(V resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (!inv.canExtract()) {
            return 0;
        }
        long totalExtracted = 0;

        for (var view : storageViews) {
            totalExtracted += view.extract(resource, maxAmount - totalExtracted, transaction);
        }

        return totalExtracted;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Iterator<StorageView<V>> iterator() {
        return (Iterator) storageViews.iterator();
    }

    private class View implements StorageView<V> {
        private final int slotIndex;

        private View(int slotIndex) {
            this.slotIndex = slotIndex;
        }

        @Override
        public long extract(V resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            if (!inv.canExtract() || !getResource().equals(resource))
                return 0;

            long actuallyExtracted = Math.min(getAmount(), maxAmount);

            if (actuallyExtracted > 0) {
                inv.updateSnapshots(slotIndex, transaction);
                var amount = getAmount() - actuallyExtracted;
                inv.beginBatch();
                if (amount <= 0) {
                    inv.setStack(slotIndex, null);
                } else {
                    inv.setStack(slotIndex, new GenericStack(conversion.getKey(resource), amount));
                }
                inv.endBatchSuppressed();
                return actuallyExtracted;
            }

            return 0;
        }

        public long insert(V resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);

            var currentKey = inv.getKey(slotIndex);
            var key = conversion.getKey(resource);
            if ((currentKey == null && inv.isAllowed(key)) || currentKey.equals(key)) {
                long inserted = Math.min(maxAmount, inv.getMaxAmount(key) - getAmount());

                if (inserted > 0) {
                    inv.updateSnapshots(slotIndex, transaction);
                    inv.beginBatch();
                    inv.setStack(slotIndex, new GenericStack(key, getAmount() + inserted));
                    inv.endBatchSuppressed();
                    return inserted;
                }
            }

            return 0;
        }

        /**
         * Checks if the slot represented by this view is actually supported by the channel.
         */
        private boolean isSupportedSlot() {
            var key = inv.getKey(slotIndex);
            return key == null || channel.tryCast(key) != null;
        }

        @Override
        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        @Override
        public V getResource() {
            return conversion.getVariant(channel.tryCast(inv.getKey(slotIndex)));
        }

        @Override
        public long getAmount() {
            return isSupportedSlot() ? inv.getAmount(slotIndex) : 0;
        }

        @Override
        public long getCapacity() {
            return isSupportedSlot() ? inv.getCapacity(conversion.getKeyType()) : 0;
        }
    }

}
