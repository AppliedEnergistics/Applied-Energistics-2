package appeng.parts.automation;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.config.Actionable;
import appeng.api.storage.data.AEKey;
import appeng.core.AELog;
import appeng.util.IVariantConversion;

class StorageImportStrategy<V extends TransferVariant<?>> implements StackImportStrategy {
    private final BlockApiCache<Storage<V>, Direction> apiCache;
    private final Direction fromSide;
    private final IVariantConversion<V> conversion;

    public StorageImportStrategy(BlockApiLookup<Storage<V>, Direction> apiLookup,
            IVariantConversion<V> conversion,
            ServerLevel level,
            BlockPos fromPos,
            Direction fromSide) {
        this.apiCache = BlockApiCache.create(apiLookup, level, fromPos);
        this.fromSide = fromSide;
        this.conversion = conversion;
    }

    @Override
    public boolean move(StackTransferContext context) {
        if (!context.isKeySpaceEnabled(conversion.getKeySpace())) {
            return false;
        }

        var adjacentStorage = apiCache.find(fromSide);
        if (adjacentStorage == null) {
            return false;
        }

        long remainingTransferAmount = context.getOperationsRemaining()
                * (long) conversion.getKeySpace().transferFactor();

        var inv = context.getInternalStorage();
        try (var tx = Transaction.openOuter()) {

            // Try to find an extractable resource that fits our filter, and if we've found at least one,
            // continue until we've filled the desired amount per transfer
            AEKey extractable = null;
            long extractableAmount = 0;
            for (var view : adjacentStorage.iterable(tx)) {
                var resource = view.getResource();
                var resourceKey = conversion.getKey(resource);
                if (resourceKey == null
                        // After the first extractable resource, we're just trying to get enough to fill our
                        // transfer quota.
                        || extractable != null && !extractable.equals(resourceKey)
                        // Regard a filter that is set on the bus
                        || !context.isInFilter(resourceKey)) {
                    continue;
                }

                // Check how much of *this* resource we can actually insert into the network, it might be 0
                // if the cells are partitioned or there's not enough types left, etc.
                var amountForThisResource = inv.insert(resourceKey, remainingTransferAmount,
                        Actionable.SIMULATE,
                        context.getActionSource());

                // Try to extract it
                var amount = view.extract(resource, amountForThisResource, tx);
                if (amount > 0) {
                    if (extractable != null) {
                        extractableAmount += amount;
                    } else {
                        extractable = resourceKey;
                        extractableAmount += amount;
                    }
                    remainingTransferAmount -= amount;
                    if (remainingTransferAmount <= 0) {
                        // We got enough to fill our transfer quota
                        break;
                    }
                }
            }

            // We might have found nothing to extract
            if (extractable == null) {
                return false;
            }

            var inserted = inv.insert(extractable, extractableAmount, Actionable.MODULATE, context.getActionSource());

            if (inserted < extractableAmount) {
                // Be nice and try to give the overflow back
                AELog.warn("Extracted %dx%s from adjacent storage and voided it because network refused insert",
                        extractableAmount - inserted, extractable);
            }

            var opsUsed = Math.max(1, inserted / conversion.getKeySpace().transferFactor());
            context.reduceOperationsRemaining(opsUsed);

            tx.commit();
            return true;
        }
    }

    public static StackImportStrategy createItem(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new StorageImportStrategy<>(
                ItemStorage.SIDED,
                IVariantConversion.ITEM,
                level,
                fromPos,
                fromSide);
    }

    public static StackImportStrategy createFluid(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new StorageImportStrategy<>(
                FluidStorage.SIDED,
                IVariantConversion.FLUID,
                level,
                fromPos,
                fromSide);
    }
}
