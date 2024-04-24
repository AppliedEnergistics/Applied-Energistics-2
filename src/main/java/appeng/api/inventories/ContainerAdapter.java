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

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Adapts an {@link InternalInventory} to the {@link Container} interface in a read-only fashion.
 */
class ContainerAdapter implements Container {
    private final InternalInventory inventory;

    public ContainerAdapter(InternalInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public int getContainerSize() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return !inventory.iterator().hasNext();
    }

    @Override
    public ItemStack getItem(int slotIndex) {
        return inventory.getStackInSlot(slotIndex);
    }

    @Override
    public void setItem(int slotIndex, ItemStack stack) {
        inventory.setItemDirect(slotIndex, stack);
    }

    @Override
    public ItemStack removeItem(int slotIndex, int count) {
        return this.inventory.extractItem(slotIndex, count, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slotIndex) {
        return this.inventory.extractItem(slotIndex, this.inventory.getSlotLimit(slotIndex), false);
    }

    /**
     * Since our inventories support a per-slot max size, we find the largest max-size allowable and return that.
     */
    @Override
    public int getMaxStackSize() {
        int max = Item.ABSOLUTE_MAX_STACK_SIZE;
        for (int i = 0; i < inventory.size(); ++i) {
            max = Math.min(max, inventory.getSlotLimit(i));
        }
        return max;
    }

    @Override
    public boolean canPlaceItem(int slotIndex, ItemStack stack) {
        return inventory.isItemValid(slotIndex, stack);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < inventory.size(); i++) {
            inventory.setItemDirect(i, ItemStack.EMPTY);
        }
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

}
