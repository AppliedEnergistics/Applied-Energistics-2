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

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.util.helpers.ItemHandlerUtil;

public class WrapperInvItemHandler implements Container {
    private final IItemHandler inv;

    public WrapperInvItemHandler(final IItemHandler inv) {
        this.inv = inv;
    }

    @Override
    public int getContainerSize() {
        return this.inv.getSlots();
    }

    @Override
    public boolean isEmpty() {
        return ItemHandlerUtil.isEmpty(this.inv);
    }

    @Override
    public ItemStack getItem(int index) {
        return this.inv.getStackInSlot(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return this.inv.extractItem(index, count, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return this.inv.extractItem(index, this.inv.getSlotLimit(index), false);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        ItemHandlerUtil.setStackInSlot(this.inv, index, stack);
    }

    @Override
    public int getMaxStackSize() {
        int max = 0;
        for (int i = 0; i < this.inv.getSlots(); ++i) {
            max = Math.max(max, this.inv.getSlotLimit(i));
        }
        return max;
    }

    @Override
    public void setChanged() {
        // NOP
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void startOpen(Player player) {
        // NOP
    }

    @Override
    public void stopOpen(Player player) {
        // NOP
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return this.inv.isItemValid(index, stack);
    }

    @Override
    public void clearContent() {
        ItemHandlerUtil.clear(this.inv);
    }

}
