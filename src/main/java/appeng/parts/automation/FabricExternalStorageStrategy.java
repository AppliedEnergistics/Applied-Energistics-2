package appeng.parts.automation;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.lookup.AEApiCache;
import appeng.api.lookup.AEApiLookup;
import appeng.api.lookup.AEApis;
import appeng.api.storage.MEStorage;
import appeng.me.storage.StorageAdapter;
import appeng.util.IVariantConversion;

public class FabricExternalStorageStrategy<V extends TransferVariant<?>> implements ExternalStorageStrategy {
    private final AEApiCache<Storage<V>> apiCache;
    private final IVariantConversion<V> conversion;

    public FabricExternalStorageStrategy(AEApiLookup<Storage<V>> apiLookup,
            IVariantConversion<V> conversion,
            ServerLevel level,
            BlockPos fromPos,
            Direction fromSide) {
        this.apiCache = apiLookup.createCache(level, fromPos, fromSide);
        this.conversion = conversion;
    }

    @Nullable
    @Override
    public MEStorage createWrapper(boolean extractableOnly, Runnable injectOrExtractCallback) {
        var storage = apiCache.find();
        if (storage == null) {
            return null;
        }

        var result = new StorageAdapter<>(conversion, storage) {
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
                AEApis.ITEMS,
                IVariantConversion.ITEM,
                level,
                fromPos,
                fromSide);
    }

    public static ExternalStorageStrategy createFluid(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new FabricExternalStorageStrategy<>(
                AEApis.FLUIDS,
                IVariantConversion.FLUID,
                level,
                fromPos,
                fromSide);
    }
}
