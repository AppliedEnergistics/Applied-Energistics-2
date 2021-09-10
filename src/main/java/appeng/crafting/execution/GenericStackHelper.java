package appeng.crafting.execution;

import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEStack;

public class GenericStackHelper {
    public static CompoundTag writeGenericStack(IAEStack<?> stack) {
        Objects.requireNonNull(stack);
        var tag = new CompoundTag();
        tag.putString("chan", stack.getChannel().getId().toString());
        stack.writeToNBT(tag);
        return tag;
    }

    public static IAEStack<?> readGenericStack(CompoundTag tag) {
        // TODO: what if a channel gets removed?
        var channel = StorageChannels.get(new ResourceLocation(tag.getString("chan")));
        return channel.createFromNBT(tag);
    }
}
