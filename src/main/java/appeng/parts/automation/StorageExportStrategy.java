package appeng.parts.automation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.util.BlockApiCache;

public class StorageExportStrategy<C, S> implements StackExportStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageExportStrategy.class);
    private final BlockApiCache<C> apiCache;
    private final Direction fromSide;
    private final HandlerStrategy<C, S> handlerStrategy;

    protected StorageExportStrategy(Capability<C> apiLookup,
            HandlerStrategy<C, S> handlerStrategy,
            ServerLevel level,
            BlockPos fromPos,
            Direction fromSide) {
        this.handlerStrategy = handlerStrategy;
        this.apiCache = BlockApiCache.create(apiLookup, level, fromPos);
        this.fromSide = fromSide;
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long amount, Actionable mode) {
        if (!handlerStrategy.isSupported(what)) {
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

        long wasInserted = handlerStrategy.insert(adjacentStorage, what, extracted, Actionable.SIMULATE);

        if (wasInserted > 0) {
            if (mode == Actionable.MODULATE) {
                extracted = StorageHelper.poweredExtraction(
                        context.getEnergySource(),
                        inv.getInventory(),
                        what,
                        wasInserted,
                        context.getActionSource(),
                        Actionable.MODULATE);

                wasInserted = handlerStrategy.insert(adjacentStorage, what, extracted, Actionable.MODULATE);

                if (wasInserted < extracted) {
                    LOGGER.error("Storage export issue, voided {}x{}", extracted - wasInserted, what);
                }
            }

            return wasInserted;
        }

        return 0;
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        if (!handlerStrategy.isSupported(what)) {
            return 0;
        }

        var adjacentStorage = apiCache.find(fromSide);
        if (adjacentStorage == null) {
            return 0;
        }

        return handlerStrategy.insert(adjacentStorage, what, amount, mode);
    }

    public static StackExportStrategy createItem(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new StorageExportStrategy<>(
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                HandlerStrategy.ITEMS,
                level,
                fromPos,
                fromSide);
    }

    public static StackExportStrategy createFluid(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new StorageExportStrategy<>(
                CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                HandlerStrategy.FLUIDS,
                level,
                fromPos,
                fromSide);
    }
}
