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
import appeng.api.storage.StorageHelper;
import appeng.api.stacks.AEKey;
import appeng.util.IVariantConversion;

class StorageExportStrategy<V extends TransferVariant<?>> implements StackExportStrategy {
    private final BlockApiCache<Storage<V>, Direction> apiCache;
    private final Direction fromSide;
    private final IVariantConversion<V> conversion;

    public StorageExportStrategy(BlockApiLookup<Storage<V>, Direction> apiLookup,
            IVariantConversion<V> conversion,
            ServerLevel level,
            BlockPos fromPos,
            Direction fromSide) {
        this.apiCache = BlockApiCache.create(apiLookup, level, fromPos);
        this.fromSide = fromSide;
        this.conversion = conversion;
    }

    @Override
    public long push(StackTransferContext context, AEKey what, long amount, Actionable mode) {
        var variant = conversion.getVariant(what);
        if (variant.isBlank()) {
            return 0;
        }

        var adjacentStorage = apiCache.find(fromSide);
        if (adjacentStorage == null) {
            return 0;
        }

        var inv = context.getInternalStorage();

        var extracted = StorageHelper.poweredExtraction(
                context.getEnergySource(),
                inv,
                what,
                amount,
                context.getActionSource(),
                Actionable.SIMULATE);

        try (var tx = Transaction.openOuter()) {
            long wasInserted = adjacentStorage.insert(variant, extracted, tx);

            if (wasInserted > 0) {
                if (mode == Actionable.MODULATE) {
                    StorageHelper.poweredExtraction(
                            context.getEnergySource(),
                            inv,
                            what,
                            wasInserted,
                            context.getActionSource(),
                            Actionable.MODULATE);
                    tx.commit();
                }

                return wasInserted;
            }
        }

        return 0;
    }

    public static StackExportStrategy createItem(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new StorageExportStrategy<>(
                ItemStorage.SIDED,
                IVariantConversion.ITEM,
                level,
                fromPos,
                fromSide);
    }

    public static StackExportStrategy createFluid(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new StorageExportStrategy<>(
                FluidStorage.SIDED,
                IVariantConversion.FLUID,
                level,
                fromPos,
                fromSide);
    }
}
