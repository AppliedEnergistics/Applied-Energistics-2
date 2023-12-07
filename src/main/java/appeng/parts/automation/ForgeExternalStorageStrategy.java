package appeng.parts.automation;

import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.storage.MEStorage;

public class ForgeExternalStorageStrategy<T, S> implements ExternalStorageStrategy {
    private final BlockCapabilityCache<T, Direction> cache;
    private final HandlerStrategy<T, S> conversion;

    public ForgeExternalStorageStrategy(BlockCapability<T, Direction> capability,
                                        HandlerStrategy<T, S> conversion,
                                        ServerLevel level,
                                        BlockPos fromPos,
                                        Direction fromSide) {
        this.cache = BlockCapabilityCache.create(capability, level, fromPos, fromSide);
        this.conversion = conversion;
    }

    @Nullable
    @Override
    public MEStorage createWrapper(boolean extractableOnly, Runnable injectOrExtractCallback) {
        var storage = cache.getCapability();
        if (storage == null) {
            return null;
        }

        var result = conversion.getFacade(storage);
        result.setChangeListener(injectOrExtractCallback);
        result.setExtractableOnly(extractableOnly);
        return result;
    }

    public static ExternalStorageStrategy createItem(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new ForgeExternalStorageStrategy<>(
                Capabilities.ItemHandler.BLOCK,
                HandlerStrategy.ITEMS,
                level,
                fromPos,
                fromSide);
    }

    public static ExternalStorageStrategy createFluid(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new ForgeExternalStorageStrategy<>(
                Capabilities.FluidHandler.BLOCK,
                HandlerStrategy.FLUIDS,
                level,
                fromPos,
                fromSide);
    }
}
