package appeng.core.api;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AppEng;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;

public final class ItemStorageChannel implements IItemStorageChannel {

    public static final IItemStorageChannel INSTANCE = new ItemStorageChannel();

    private ItemStorageChannel() {
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return AppEng.makeId("item");
    }

    @Override
    public IItemList<IAEItemStack> createList() {
        return new ItemList();
    }

    @Override
    public IAEItemStack createStack(Object input) {
        Preconditions.checkNotNull(input);

        if (input instanceof ItemStack) {
            return AEItemStack.fromItemStack((ItemStack) input);
        }

        return null;
    }

    @Override
    public IAEItemStack createFromNBT(CompoundTag nbt) {
        Preconditions.checkNotNull(nbt);
        return AEItemStack.fromNBT(nbt);
    }

    @Override
    public IAEItemStack readFromPacket(FriendlyByteBuf input) {
        Preconditions.checkNotNull(input);

        return AEItemStack.fromPacket(input);
    }
}
