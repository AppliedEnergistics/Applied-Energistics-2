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

import com.google.common.base.Preconditions;

import net.minecraft.world.item.ItemStack;

/**
 * Exposes a subset of an {@link InternalInventory}.
 */
final class SubInventoryProxy extends BaseInternalInventory {
    private final InternalInventory delegate;
    private final int fromSlot;
    private final int toSlot;

    public SubInventoryProxy(InternalInventory delegate, int fromSlotInclusive, int toSlotExclusive) {
        Preconditions.checkArgument(fromSlotInclusive <= toSlotExclusive, "fromSlotInclusive <= toSlotExclusive");
        Preconditions.checkArgument(fromSlotInclusive >= 0, "fromSlotInclusive >= 0");
        Preconditions.checkArgument(toSlotExclusive <= delegate.size(), "toSlotExclusive <= size()");
        this.delegate = delegate;
        this.fromSlot = fromSlotInclusive;
        this.toSlot = toSlotExclusive;
    }

    @Override
    public int size() {
        return toSlot - fromSlot;
    }

    private int translateSlot(int slotIndex) {
        Preconditions.checkArgument(slotIndex >= 0, "slotIndex >= 0");
        Preconditions.checkArgument(slotIndex < size(), "slotIndex < size()");
        return slotIndex + fromSlot;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return delegate.getStackInSlot(translateSlot(slotIndex));
    }

    @Override
    public void setItemDirect(int slotIndex, ItemStack stack) {
        delegate.setItemDirect(translateSlot(slotIndex), stack);
    }

    @Override
    public InternalInventory getSubInventory(int fromSlotInclusive, int toSlotExclusive) {
        Preconditions.checkArgument(toSlotExclusive >= 0, "toSlotExclusive >= 0");
        Preconditions.checkArgument(toSlotExclusive <= size(), "toSlotExclusive <= size()");
        return delegate.getSubInventory(translateSlot(fromSlotInclusive), toSlotExclusive + this.fromSlot);
    }

    @Override
    public InternalInventory getSlotInv(int slotIndex) {
        return delegate.getSlotInv(translateSlot(slotIndex));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return delegate.insertItem(translateSlot(slot), stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return delegate.extractItem(translateSlot(slot), amount, simulate);
    }

    @Override
    public void sendChangeNotification(int slot) {
        delegate.sendChangeNotification(translateSlot(slot));
    }
}
