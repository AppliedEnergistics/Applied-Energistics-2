/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.util.inv;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;

/**
 * Exposes several internal inventories as one larger internal inventory.
 */
public class CombinedInternalInventory extends BaseInternalInventory {
    private final InternalInventory[] inventories; // the handlers
    private final int[] baseIndex; // index-offsets of the different handlers
    private final int slotCount; // number of total slots

    public CombinedInternalInventory(InternalInventory... inventories) {
        this.inventories = inventories;
        this.baseIndex = new int[this.inventories.length];
        int index = 0;
        for (int i = 0; i < this.inventories.length; i++) {
            index += this.inventories[i].size();
            this.baseIndex[i] = index;
        }
        this.slotCount = index;
    }

    // returns the handler index for the slot
    private int getIndexForSlot(int slot) {
        if (slot < 0) {
            return -1;
        }

        for (int i = 0; i < this.baseIndex.length; i++) {
            if (slot - this.baseIndex[i] < 0) {
                return i;
            }
        }
        return -1;
    }

    private InternalInventory getHandlerFromIndex(int index) {
        if (index < 0 || index >= this.inventories.length) {
            return InternalInventory.empty();
        }
        return this.inventories[index];
    }

    private int getSlotFromIndex(int slot, int index) {
        if (index <= 0 || index >= this.baseIndex.length) {
            return slot;
        }
        return slot - this.baseIndex[index - 1];
    }

    @Override
    public int size() {
        return this.slotCount;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        int index = this.getIndexForSlot(slot);
        var handler = this.getHandlerFromIndex(index);
        int targetSlot = this.getSlotFromIndex(slot, index);
        return handler.getStackInSlot(targetSlot);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        int index = this.getIndexForSlot(slot);
        var handler = this.getHandlerFromIndex(index);
        int targetSlot = this.getSlotFromIndex(slot, index);
        return handler.insertItem(targetSlot, stack, simulate);
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        int index = this.getIndexForSlot(slot);
        var handler = this.getHandlerFromIndex(index);
        int targetSlot = this.getSlotFromIndex(slot, index);
        return handler.extractItem(targetSlot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        int index = this.getIndexForSlot(slot);
        var handler = this.getHandlerFromIndex(index);
        int localSlot = this.getSlotFromIndex(slot, index);
        return handler.getSlotLimit(localSlot);
    }

    @Override
    public void setItemDirect(int slot, ItemStack stack) {
        int index = this.getIndexForSlot(slot);
        var handler = this.getHandlerFromIndex(index);
        int targetSlot = this.getSlotFromIndex(slot, index);
        handler.setItemDirect(targetSlot, stack);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        int index = this.getIndexForSlot(slot);
        var handler = this.getHandlerFromIndex(index);
        int targetSlot = this.getSlotFromIndex(slot, index);
        return handler.isItemValid(targetSlot, stack);
    }
}
