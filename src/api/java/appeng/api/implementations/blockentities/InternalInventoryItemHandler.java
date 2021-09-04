package appeng.api.implementations.blockentities;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

class InternalInventoryItemHandler implements IItemHandlerModifiable {
    private final InternalInventory inventory;

    public InternalInventoryItemHandler(InternalInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public int getSlots() {
        return inventory.size();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        inventory.setItemDirect(slot, stack);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSlotLimit(int slot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        throw new UnsupportedOperationException();
    }
}
