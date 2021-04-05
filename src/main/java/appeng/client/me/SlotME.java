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
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEItemStack;

public class SlotME extends Slot {

    private static final IInventory EMPTY_INVENTORY = new Inventory(0);

    private final InternalSlotME slot;

    public SlotME(final InternalSlotME slot) {
        super(EMPTY_INVENTORY, 0, slot.getxPosition(), slot.getyPosition());
        this.slot = slot;
    }

    public IAEItemStack getAEStack() {
        if (this.slot.hasPower()) {
            return this.slot.getAEStack();
        }
        return null;
    }

    @Override
    public boolean isItemValid(final ItemStack par1ItemStack) {
        return false;
    }

    @Override
    public ItemStack getStack() {
        if (this.slot.hasPower()) {
            return this.slot.getStack();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean getHasStack() {
        if (this.slot.hasPower()) {
            return !this.getStack().isEmpty();
        }
        return false;
    }

    @Override
    public void putStack(final ItemStack par1ItemStack) {

    }

    @Override
    public int getSlotStackLimit() {
        return 0;
    }

    @Override
    public ItemStack decrStackSize(final int par1) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakeStack(final PlayerEntity par1PlayerEntity) {
        return false;
    }
}
