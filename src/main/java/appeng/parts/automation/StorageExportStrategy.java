package appeng.parts.automation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.util.IVariantConversion;
import appeng.util.Platform;

class StorageExportStrategy<V extends TransferVariant<?>> implements StackExportStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageExportStrategy.class);
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
    public long transfer(StackTransferContext context, AEKey what, long amount, Actionable mode) {
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
                inv.getInventory(),
                what,
                amount,
                context.getActionSource(),
                Actionable.SIMULATE);

        if (extracted <= 0) {
            return 0;
        }

        // Determine how much we're allowed to insert
        long wasInserted;
        try (var tx = Platform.openOrJoinTx()) {
            wasInserted = adjacentStorage.insert(variant, extracted, tx);
        }

        if (wasInserted > 0 && mode == Actionable.MODULATE) {
            // Now *really* extract if we're modulating because the simulate may have
            // returned more items than we can actually get (i.e. two storage buses
            // on the same chest).
            extracted = StorageHelper.poweredExtraction(
                    context.getEnergySource(),
                    inv.getInventory(),
                    what,
                    wasInserted,
                    context.getActionSource(),
                    Actionable.MODULATE);

            try (var tx = Platform.openOrJoinTx()) {
                wasInserted = adjacentStorage.insert(variant, extracted, tx);
                if (wasInserted < extracted) {
                    LOGGER.error("Storage export issue, voided {}x{}", extracted - wasInserted, what);
                }
                tx.commit();
            }
        }

        return wasInserted;
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        var variant = conversion.getVariant(what);
        if (variant.isBlank()) {
            return 0;
        }

        var adjacentStorage = apiCache.find(fromSide);
        if (adjacentStorage == null) {
            return 0;
        }

        try (var tx = Platform.openOrJoinTx()) {
            long wasInserted = adjacentStorage.insert(variant, amount, tx);

            if (wasInserted > 0) {
                if (mode == Actionable.MODULATE) {
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
