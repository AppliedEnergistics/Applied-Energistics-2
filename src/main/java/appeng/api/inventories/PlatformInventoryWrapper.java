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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Wraps an inventory implementing ResourceHandler such that it can be used as an {@link InternalInventory}.
 * 
 * @deprecated We need to find a better abstraction of this since we use InternalInventory for UIs too, which still need
 *             direct mutable ItemStack access
 */
public class PlatformInventoryWrapper implements InternalInventory {
    private final ResourceHandler<ItemResource> handler;

    public PlatformInventoryWrapper(ResourceHandler<ItemResource> handler) {
        this.handler = handler;
    }

    @Override
    public ResourceHandler<ItemResource> toResourceHandler() {
        return handler;
    }

    @Override
    public int size() {
        return handler.size();
    }

    @Override
    public int getSlotLimit(int slot) {
        return handler.getCapacityAsInt(slot, ItemResource.EMPTY);
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        // TODO 1.21.9: this is obviously not mutable
        var resource = handler.getResource(slotIndex);
        var amount = handler.getAmountAsInt(slotIndex);
        if (!resource.isEmpty()) {
            return resource.toStack(amount);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void setItemDirect(int slotIndex, ItemStack stack) {
        try (var tx = Transaction.open(null)) {
            var current = handler.getResource(slotIndex);
            if (!current.isEmpty()) {
                handler.extract(slotIndex, current, handler.getAmountAsInt(slotIndex), tx);
            }
            handler.insert(slotIndex, ItemResource.of(stack), stack.getCount(), tx);
            tx.commit();
        }
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return handler.isValid(slot, ItemResource.of(stack));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        try (var tx = Transaction.open(null)) {
            var inserted = handler.insert(slot, ItemResource.of(stack), stack.getCount(), tx);
            if (!simulate) {
                tx.commit();
            }
            return stack.copyWithCount(stack.getCount() - inserted);
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        try (var tx = Transaction.open(null)) {
            var resource = handler.getResource(slot);
            if (resource.isEmpty()) {
                return ItemStack.EMPTY;
            }
            var extracted = handler.extract(slot, resource, amount, tx);
            if (!simulate) {
                tx.commit();
            }
            return resource.toStack(extracted);
        }
    }

}
