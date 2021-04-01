package appeng.client.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * A proxy for a slot that will always return an itemstack with size 1, if there is an item in the slot. Used to prevent
 * the default item count from rendering.
 */
class Size1Slot extends Slot {

    private final Slot delegate;

    public Size1Slot(Slot delegate) {
        super(delegate.inventory, -1, delegate.xPos, delegate.yPos);
        this.delegate = delegate;
    }

    @Override
    @Nonnull
    public ItemStack getStack() {
        ItemStack orgStack = this.delegate.getStack();
        if (!orgStack.isEmpty()) {
            ItemStack modifiedStack = orgStack.copy();
            modifiedStack.setCount(1);
            return modifiedStack;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean getHasStack() {
        return this.delegate.getHasStack();
    }

    @Override
    public int getSlotStackLimit() {
        return this.delegate.getSlotStackLimit();
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return this.delegate.getItemStackLimit(stack);
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) {
        return this.delegate.canTakeStack(playerIn);
    }

    @Override
    public void onSlotChanged() {
        delegate.onSlotChanged();
    }

    @Override
    @Environment(EnvType.CLIENT)
    @Nullable
    public Pair<ResourceLocation, ResourceLocation> getBackground() {
        return delegate.getBackground();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean isEnabled() {
        return delegate.isEnabled();
    }

}
