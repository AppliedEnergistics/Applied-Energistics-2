package appeng.parts.automation;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

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
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                HandlerStrategy.ITEMS,
                level,
                fromPos,
                fromSide);
    }

    public static ExternalStorageStrategy createFluid(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new ForgeExternalStorageStrategy<>(
                CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                HandlerStrategy.FLUIDS,
                level,
                fromPos,
                fromSide);
    }
}
