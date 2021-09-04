package appeng.api.implementations.blockentities;

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.minecraft.world.item.ItemStack;

class InternalInventoryIterator implements Iterator<ItemStack> {
    private final InternalInventory inventory;
    private int currentSlot;
    private ItemStack currentStack;

    InternalInventoryIterator(InternalInventory inventory) {
        this.inventory = inventory;
        currentSlot = -1;
        seekNext();
    }

    private void seekNext() {
        currentStack = ItemStack.EMPTY;
        for (currentSlot++; currentSlot < inventory.size(); currentSlot++) {
            currentStack = inventory.getStackInSlot(currentSlot);
            if (!currentStack.isEmpty()) {
                break;
            }
        }
    }

    @Override
    public boolean hasNext() {
        return !currentStack.isEmpty();
    }

    @Override
    public ItemStack next() {
        if (currentStack.isEmpty()) {
            throw new NoSuchElementException();
        }
        var result = currentStack;
        seekNext();
        return result;
    }
}
