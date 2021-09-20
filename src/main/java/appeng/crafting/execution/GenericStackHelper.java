package appeng.crafting.execution;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEStack;

public class GenericStackHelper {
    public static CompoundTag writeGenericStack(IAEStack stack) {
        if (stack == null) {
            return new CompoundTag();
        }
        var tag = new CompoundTag();
        tag.putString("chan", stack.getChannel().getId().toString());
        stack.writeToNBT(tag);
        return tag;
    }

    public static IAEStack readGenericStack(CompoundTag tag) {
        if (tag.isEmpty()) {
            return null;
        }
        // TODO: what if a channel gets removed?
        var channel = StorageChannels.get(new ResourceLocation(tag.getString("chan")));
        return channel.createFromNBT(tag);
    }

    public static IAEStack injectMonitorable(IStorageMonitorable monitorable, IAEStack what, Actionable mode,
            IActionSource src) {
        if (what == null) {
            return null;
        }
        return channelInjectItems(what.getChannel(), monitorable, what, mode, src);
    }

    private static <T extends IAEStack> T channelInjectItems(IStorageChannel<T> channel,
            IStorageMonitorable monitorable, IAEStack what, Actionable type, IActionSource src) {
        var castedWhat = what.cast(channel);
        var inventory = monitorable.getInventory(channel);
        if (inventory != null) {
            return inventory.injectItems(castedWhat, type, src);
        }
        return null;
    }
}
