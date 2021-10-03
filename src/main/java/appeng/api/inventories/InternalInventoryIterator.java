/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.inventories;

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.minecraft.world.item.ItemStack;

/**
 * Iterates over the slots of an {@link InternalInventory} that are not empty.
 */
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
