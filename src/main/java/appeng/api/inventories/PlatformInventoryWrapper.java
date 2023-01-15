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

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Wraps an inventory implementing the platforms standard inventory interface (i.e. IItemHandler on Forge) such that it
 * can be used as an {@link InternalInventory}.
 */
public class PlatformInventoryWrapper implements InternalInventory {
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
    public void setItemDirect(int slotIndex, ItemStack stack) {
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

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return handler.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return handler.extractItem(slot, amount, simulate);
    }

}
