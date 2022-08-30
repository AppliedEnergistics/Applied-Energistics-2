package appeng.parts.automation;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.storage.MEStorage;
import appeng.me.storage.StorageAdapter;
import appeng.util.IVariantConversion;

public class FabricExternalStorageStrategy<V extends TransferVariant<?>> implements ExternalStorageStrategy {
    private final BlockApiCache<Storage<V>, Direction> apiCache;
    private final Direction fromSide;
    private final IVariantConversion<V> conversion;

    public FabricExternalStorageStrategy(BlockApiLookup<Storage<V>, Direction> apiLookup,
            IVariantConversion<V> conversion,
            ServerLevel level,
            BlockPos fromPos,
            Direction fromSide) {
        this.apiCache = BlockApiCache.create(apiLookup, level, fromPos);
        this.fromSide = fromSide;
        this.conversion = conversion;
    }

    @Nullable
    @Override
    public MEStorage createWrapper(boolean extractableOnly, Runnable injectOrExtractCallback) {
        var storage = apiCache.find(fromSide);
        if (storage == null) {
            // If storage is absent, never query again until the next update.
            return null;
        }

        var result = new StorageAdapter<>(conversion, () -> apiCache.find(fromSide)) {
            @Override
            protected void onInjectOrExtract() {
                injectOrExtractCallback.run();
            }
        };
        result.setExtractableOnly(extractableOnly);
        return result;
    }

    public static ExternalStorageStrategy createItem(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new FabricExternalStorageStrategy<>(
                ItemStorage.SIDED,
                IVariantConversion.ITEM,
                level,
                fromPos,
                fromSide);
    }

    public static ExternalStorageStrategy createFluid(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new FabricExternalStorageStrategy<>(
                FluidStorage.SIDED,
                IVariantConversion.FLUID,
                level,
                fromPos,
                fromSide);
    }
}
