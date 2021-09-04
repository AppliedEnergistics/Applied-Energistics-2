package appeng.util.inv;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.blockentities.InternalInventory;
import appeng.util.Platform;

public class CarriedItemInventory implements InternalInventory {
    private final AbstractContainerMenu menu;

    public CarriedItemInventory(AbstractContainerMenu menu) {
        this.menu = menu;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        Preconditions.checkArgument(slotIndex == 0);
        return menu.getCarried();
    }

    @Override
    public void setItemDirect(int slotIndex, @Nonnull ItemStack stack) {
        Preconditions.checkArgument(slotIndex == 0);
        menu.setCarried(stack);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return stack;
        }

        var inSlot = getStackInSlot(slot);
        if (inSlot.isEmpty()) {
            if (!simulate) {
                setItemDirect(slot, stack);
            }
            return ItemStack.EMPTY;
        }

        if (!Platform.itemComparisons().isSameItem(inSlot, stack)) {
            return stack;
        }

        int freeSpace = inSlot.getMaxStackSize() - inSlot.getCount();
        if (freeSpace <= 0) {
            return stack;
        }

        if (!simulate) {
            inSlot.grow(Math.min(stack.getCount(), freeSpace));
        }

        if (freeSpace >= stack.getCount()) {
            return ItemStack.EMPTY;
        } else {
            var r = stack.copy();
            r.shrink(freeSpace);
            return r;
        }
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        var item = getStackInSlot(slot);
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (amount >= item.getCount()) {
            if (!simulate) {
                setItemDirect(slot, ItemStack.EMPTY);
                return item;
            } else {
                return item.copy();
            }
        } else {
            var result = item.copy();
            result.setCount(amount);

            if (!simulate) {
                var reduced = item.copy();
                reduced.shrink(amount);
                setItemDirect(slot, reduced);
            }
            return result;
        }
    }
}
