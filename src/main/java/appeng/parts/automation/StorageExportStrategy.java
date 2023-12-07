package appeng.parts.automation;

import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;

public class StorageExportStrategy<T, S> implements StackExportStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageExportStrategy.class);
    private final BlockCapabilityCache<T, Direction> cache;
    private final HandlerStrategy<T, S> handlerStrategy;

    protected StorageExportStrategy(BlockCapability<T, Direction> capability,
                                    HandlerStrategy<T, S> handlerStrategy,
                                    ServerLevel level,
                                    BlockPos fromPos,
                                    Direction fromSide) {
        this.handlerStrategy = handlerStrategy;
        this.cache = BlockCapabilityCache.create(capability, level, fromPos, fromSide);
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long amount) {
        if (!handlerStrategy.isSupported(what)) {
            return 0;
        }

        var adjacentStorage = cache.getCapability();
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

        long wasInserted = handlerStrategy.insert(adjacentStorage, what, extracted, Actionable.SIMULATE);

        if (wasInserted > 0) {
            extracted = StorageHelper.poweredExtraction(
                    context.getEnergySource(),
                    inv.getInventory(),
                    what,
                    wasInserted,
                    context.getActionSource(),
                    Actionable.MODULATE);

            wasInserted = handlerStrategy.insert(adjacentStorage, what, extracted, Actionable.MODULATE);

            if (wasInserted < extracted) {
                // Be nice and try to give the overflow back
                long leftover = extracted - wasInserted;
                leftover -= inv.getInventory().insert(what, leftover, Actionable.MODULATE, context.getActionSource());
                if (leftover > 0) {
                    LOGGER.error("Storage export: adjacent block unexpectedly refused insert, voided {}x{}", leftover,
                            what);
                }
            }
        }

        return wasInserted;
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        if (!handlerStrategy.isSupported(what)) {
            return 0;
        }

        var adjacentStorage = cache.getCapability();
        if (adjacentStorage == null) {
            return 0;
        }

        return handlerStrategy.insert(adjacentStorage, what, amount, mode);
    }

    public static StackExportStrategy createItem(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new StorageExportStrategy<>(
                Capabilities.ItemHandler.BLOCK,
                HandlerStrategy.ITEMS,
                level,
                fromPos,
                fromSide);
    }

    public static StackExportStrategy createFluid(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new StorageExportStrategy<>(
                Capabilities.FluidHandler.BLOCK,
                HandlerStrategy.FLUIDS,
                level,
                fromPos,
                fromSide);
    }
}
