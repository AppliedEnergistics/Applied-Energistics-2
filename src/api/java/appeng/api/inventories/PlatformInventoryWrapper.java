package appeng.api.inventories;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Wraps an inventory implementing the platforms standard inventory interface (i.e. IItemHandler on Forge) such that it
 * can be used as an {@link InternalInventory}.
 */
class PlatformInventoryWrapper implements InternalInventory {
    private final IItemHandler handler;

    public PlatformInventoryWrapper(IItemHandler handler) {
        this.handler = handler;
    }

    @Override
    public IItemHandler toItemHandler() {
        return handler;
    }

    @Override
    public int size() {
        return handler.getSlots();
    }

    @Override
    public int getSlotLimit(int slot) {
        return handler.getSlotLimit(slot);
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return handler.getStackInSlot(slotIndex);
    }

    @Override
    public void setItemDirect(int slotIndex, @Nonnull ItemStack stack) {
        if (handler instanceof IItemHandlerModifiable modifiableHandler) {
            modifiableHandler.setStackInSlot(slotIndex, stack);
        } else {
            handler.extractItem(slotIndex, Integer.MAX_VALUE, false);
            handler.insertItem(slotIndex, stack, false);
        }
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return handler.isItemValid(slot, stack);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return handler.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return handler.extractItem(slot, amount, simulate);
    }
}
