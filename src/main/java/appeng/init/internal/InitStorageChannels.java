package appeng.init.internal;

import appeng.api.storage.StorageChannels;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.core.api.FluidStorageChannel;
import appeng.core.api.ItemStorageChannel;

public final class InitStorageChannels {

    private InitStorageChannels() {
    }

    public static void init() {
        StorageChannels.register(IItemStorageChannel.class, ItemStorageChannel.INSTANCE);
        StorageChannels.register(IFluidStorageChannel.class, FluidStorageChannel.INSTANCE);
    }

}
