package appeng.parts.automation;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.storage.MEStorage;
import appeng.util.BlockApiCache;

public class ForgeExternalStorageStrategy<C, S> implements ExternalStorageStrategy {
    private final BlockApiCache<C> apiCache;
    private final Direction fromSide;
    private final HandlerStrategy<C, S> conversion;

    public ForgeExternalStorageStrategy(Capability<C> capability,
            HandlerStrategy<C, S> conversion,
            ServerLevel level,
            BlockPos fromPos,
            Direction fromSide) {
        this.apiCache = BlockApiCache.create(capability, level, fromPos);
        this.fromSide = fromSide;
        this.conversion = conversion;
    }

    @Nullable
    @Override
    public MEStorage createWrapper(boolean extractableOnly, Runnable injectOrExtractCallback) {
        var storage = apiCache.find(fromSide);
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
                ForgeCapabilities.ITEM_HANDLER,
                HandlerStrategy.ITEMS,
                level,
                fromPos,
                fromSide);
    }

    public static ExternalStorageStrategy createFluid(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new ForgeExternalStorageStrategy<>(
                ForgeCapabilities.FLUID_HANDLER,
                HandlerStrategy.FLUIDS,
                level,
                fromPos,
                fromSide);
    }
}
