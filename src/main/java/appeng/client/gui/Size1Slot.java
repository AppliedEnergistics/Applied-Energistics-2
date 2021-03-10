
package appeng.client.gui;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A proxy for a slot that will always return an itemstack with size 1, if there is an item in the slot. Used to prevent
 * the default item count from rendering.
 */
class Size1Slot extends Slot {

    private final Slot delegate;

    public Size1Slot(Slot delegate) {
        super(delegate.container, delegate.getSlotIndex(), delegate.x, delegate.y);
        this.delegate = delegate;
    }

    @Override
    @Nonnull
    public ItemStack getItem() {
        ItemStack orgStack = this.delegate.getItem();
        if (!orgStack.isEmpty()) {
            ItemStack modifiedStack = orgStack.copy();
            modifiedStack.setCount(1);
            return modifiedStack;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean hasItem() {
        return this.delegate.hasItem();
    }

    @Override
    public int getMaxStackSize() {
        return this.delegate.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return this.delegate.getMaxStackSize(stack);
    }

    @Override
    public boolean mayPickup(PlayerEntity playerIn) {
        return this.delegate.mayPickup(playerIn);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isActive() {
        return this.delegate.isActive();
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
