/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.client.me;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import appeng.api.storage.data.IAEItemStack;

public class SlotME extends Slot {

    private final InternalSlotME mySlot;

    public SlotME(final InternalSlotME me) {
        super(null, 0, me.getxPosition(), me.getyPosition());
        this.mySlot = me;
    }

    public IAEItemStack getAEStack() {
        if (this.mySlot.hasPower()) {
            return this.mySlot.getAEStack();
        }
        return null;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack getStack() {
        if (this.mySlot.hasPower()) {
            return this.mySlot.getStack();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean hasStack() {
        if (this.mySlot.hasPower()) {
            return !this.getStack().isEmpty();
        }
        return false;
    }

    @Override
    public void setStack(ItemStack stack) {
    }

    @Override
    public int getMaxItemCount() {
        return 0;
    }

    @Override
    public ItemStack takeStack(final int par1) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakeItems(final PlayerEntity par1PlayerEntity) {
        return false;
    }
}
