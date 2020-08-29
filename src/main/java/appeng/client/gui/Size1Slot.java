
package appeng.client.gui;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A proxy for a slot that will always return an itemstack with size 1, if there
 * is an item in the slot. Used to prevent the default item count from
 * rendering.
 */
class Size1Slot extends Slot {

    private final Slot delegate;

    public Size1Slot(Slot delegate) {
        super(delegate.inventory, delegate.getSlotIndex(), delegate.xPos, delegate.yPos);
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
    @OnlyIn(Dist.CLIENT)
    public boolean isEnabled() {
        return this.delegate.isEnabled();
    }

    @Override
    public int getSlotIndex() {
        return this.delegate.getSlotIndex();
    }

    @Override
    public boolean isSameInventory(Slot other) {
        return this.delegate.isSameInventory(other);
    }
}
