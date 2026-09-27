package appeng.helpers.externalstorage;

import com.google.common.primitives.Ints;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.helpers.ResourceConversion;

/**
 * Adapts a {@link GenericStackInv} as {@link ResourceHandler} of the appropriate type.
 */
public class GenericStackInvHandler<V extends Resource> implements ResourceHandler<V> {
    private final ResourceConversion<V> conversion;
    private final GenericInternalInventory inv;
    private final AEKeyType channel;

    public GenericStackInvHandler(ResourceConversion<V> conversion, AEKeyType channel,
            GenericInternalInventory inv) {
        this.conversion = conversion;
        this.channel = channel;
        this.inv = inv;
    }

    /**
     * Checks if the slot represented by this view is actually supported by the channel.
     */
    private boolean isSupportedSlot(int index) {
        var key = inv.getKey(index);
        return key == null || channel.tryCast(key) != null;
    }

    @Override
    public int insert(V resource, int maxAmount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, maxAmount);
        if (!inv.canInsert()) {
            return 0;
        }
        int totalInserted = 0;

        // First iteration matches the resource, second iteration inserts into empty slots.
        int size = size();
        for (int i = 0; i < size; i++) {
            if (inv.getKey(i) != null) {
                totalInserted += insert(i, resource, maxAmount - totalInserted, transaction);
                if (totalInserted >= maxAmount) {
                    break;
                }
            }
        }

        for (int i = 0; i < size; i++) {
            if (inv.getKey(i) == null) {
                totalInserted += insert(i, resource, maxAmount - totalInserted, transaction);
                if (totalInserted >= maxAmount) {
                    break;
                }
            }
        }

        return totalInserted;
    }

    @Override
    public int extract(V resource, int maxAmount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, maxAmount);
        if (!inv.canExtract()) {
            return 0;
        }
        return ResourceHandler.super.extract(resource, maxAmount, transaction);
    }

    @Override
    public int size() {
        return inv.size();
    }

    @Override
    public V getResource(int index) {
        return conversion.getVariant(inv.getKey(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        if (!isSupportedSlot(index)) {
            return 0;
        }
        return inv.getAmount(index);
    }

    @Override
    public long getCapacityAsLong(int index, V resource) {
        if (!isSupportedSlot(index)) {
            return 0;
        }
        if (!resource.isEmpty()) {
            return inv.getMaxAmount(conversion.getKey(resource));
        }
        return inv.getCapacity(conversion.getKeyType());
    }

    @Override
    public boolean isValid(int index, V resource) {
        TransferPreconditions.checkNonEmpty(resource);
        return inv.isAllowedIn(index, conversion.getKey(resource));
    }

    @Override
    public int insert(int index, V resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (!inv.canInsert()) {
            return 0;
        }

        var currentKey = inv.getKey(index);
        var key = conversion.getKey(resource);
        if ((currentKey == null && inv.isAllowedIn(index, key)) || (currentKey != null && currentKey.equals(key))) {
            int inserted = (int) Math.min(amount, inv.getMaxAmount(key) - getAmountAsLong(index));

            if (inserted > 0) {
                inv.updateSnapshots(transaction);
                inv.beginBatch();
                inv.setStack(index, new GenericStack(key, getAmountAsLong(index) + inserted));
                inv.endBatchSuppressed();
                return inserted;
            }
        }

        return 0;
    }

    @Override
    public int extract(int index, V resource, int maxAmount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, maxAmount);
        if (!inv.canExtract() || !getResource(index).equals(resource))
            return 0;

        int actuallyExtracted = Ints.saturatedCast(Math.min(inv.getAmount(index), maxAmount));

        if (actuallyExtracted > 0) {
            inv.updateSnapshots(transaction);
            var remainder = inv.getAmount(index) - actuallyExtracted;
            inv.beginBatch();
            if (remainder <= 0) {
                inv.setStack(index, null);
            } else {
                inv.setStack(index, new GenericStack(conversion.getKey(resource), remainder));
            }
            inv.endBatchSuppressed();
            return actuallyExtracted;
        }

        return 0;
    }
}
