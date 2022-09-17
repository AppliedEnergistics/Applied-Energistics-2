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


import appeng.util.helpers.ItemHandlerUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;


public class WrapperRangeItemHandler implements IItemHandlerModifiable {
    private final IItemHandler compose;
    private final int minSlot;
    private final int maxSlot;

    public WrapperRangeItemHandler(IItemHandler compose, int minSlot, int maxSlotExclusive) {
        this.compose = compose;
        this.minSlot = minSlot;
        this.maxSlot = maxSlotExclusive;
    }

    @Override
    public int getSlots() {
        return this.maxSlot - this.minSlot;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        if (this.checkSlot(slot)) {
            return this.compose.getStackInSlot(slot + this.minSlot);
        }

        return ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (this.checkSlot(slot)) {
            return this.compose.insertItem(slot + this.minSlot, stack, simulate);
        }

        return stack;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (this.checkSlot(slot)) {
            return this.compose.extractItem(slot + this.minSlot, amount, simulate);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (this.checkSlot(slot)) {
            ItemHandlerUtil.setStackInSlot(this.compose, slot + this.minSlot, stack);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        if (this.checkSlot(slot)) {
            return this.compose.getSlotLimit(slot + this.minSlot);
        }

        return 0;
    }

    private boolean checkSlot(int localSlot) {
        return localSlot + this.minSlot < this.maxSlot;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (this.checkSlot(slot)) {
            return this.compose.isItemValid(slot + this.minSlot, stack);
        }
        return false;
    }
}
