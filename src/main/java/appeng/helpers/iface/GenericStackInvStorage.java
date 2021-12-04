package appeng.helpers.iface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.Items;

import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.AEItemKey;
import appeng.util.IVariantConversion;

/**
 * Adapts a {@link GenericStackInv} as {@link net.fabricmc.fabric.api.transfer.v1.storage.Storage} of the appropriate
 * type.
 */
public class GenericStackInvStorage<V extends TransferVariant<?>> implements Storage<V> {
    private final IVariantConversion<V> conversion;
    private final GenericStackInv inv;
    private final AEKeyType channel;
    private final List<View> storageViews;

    public GenericStackInvStorage(IVariantConversion<V> conversion, AEKeyType channel,
            GenericStackInv inv) {
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
        long totalExtracted = 0;

        for (var view : storageViews) {
            totalExtracted += view.extract(resource, maxAmount - totalExtracted, transaction);
        }

        return totalExtracted;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Iterator<StorageView<V>> iterator(TransactionContext transaction) {
        return (Iterator) storageViews.iterator();
    }

    /**
     * Exports the item content of the given inventory as storage.
     */
    public static GenericStackInvStorage<ItemVariant> items(GenericStackInv inv) {
        return new GenericStackInvStorage<>(IVariantConversion.ITEM, AEKeyType.items(), inv);
    }

    /**
     * Exports the fluid content of the given inventory as storage.
     */
    public static GenericStackInvStorage<FluidVariant> fluids(GenericStackInv inv) {
        return new GenericStackInvStorage<>(IVariantConversion.FLUID, AEKeyType.fluids(), inv);
    }

    private class View extends SnapshotParticipant<GenericStack> implements StorageView<V> {
        // SnapshotParticipant doesn't allow null snapshots so we use this marker stack
        private static final GenericStack EMPTY_STACK = new GenericStack(AEItemKey.of(Items.AIR), 0);

        private final int slotIndex;

        private View(int slotIndex) {
            this.slotIndex = slotIndex;
        }

        @Override
        public long extract(V resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            if (!getResource().equals(resource))
                return 0;

            long actuallyExtracted = Math.min(getAmount(), maxAmount);

            if (actuallyExtracted > 0) {
                updateSnapshots(transaction);
                var amount = getAmount() - actuallyExtracted;
                inv.beginBatch();
                if (amount <= 0) {
                    inv.setStack(slotIndex, null);
                } else {
                    inv.setStack(slotIndex, new GenericStack(conversion.getKey(resource), amount));
                }
                inv.endBatchSuppressed();
            }

            return actuallyExtracted;
        }

        public long insert(V resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);

            var currentKey = inv.getKey(slotIndex);
            var key = conversion.getKey(resource);
            if (currentKey == null || currentKey.equals(key)) {
                long inserted = Math.min(maxAmount, inv.getMaxAmount(key) - getAmount());

                if (inserted > 0) {
                    updateSnapshots(transaction);
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
            return isSupportedSlot() && inv.getStack(slotIndex) == null;
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

        @Override
        protected GenericStack createSnapshot() {
            var stack = inv.getStack(slotIndex);
            return stack != null ? stack : EMPTY_STACK;
        }

        @Override
        protected void readSnapshot(GenericStack snapshot) {
            if (snapshot == EMPTY_STACK) {
                inv.setStack(slotIndex, null);
            } else {
                inv.setStack(slotIndex, snapshot);
            }
        }

        @Override
        protected void onFinalCommit() {
            inv.onChange();
        }
    }

}
