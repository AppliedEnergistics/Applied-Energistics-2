/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.container.slots.IMEFluidSlot;

public class SlotFluidME extends Slot implements IMEFluidSlot {

    private static final IInventory EMPTY_INVENTORY = new Inventory(0);

    private final InternalFluidSlotME slot;

    public SlotFluidME(InternalFluidSlotME slot) {
        super(EMPTY_INVENTORY, 0, slot.getxPosition(), slot.getyPosition());
        this.slot = slot;
    }

    @Override
    public IAEFluidStack getAEFluidStack() {
        if (this.slot.hasPower()) {
            return this.slot.getAEStack();
        }
        return null;
    }

    @Override
    public boolean mayPlace(final ItemStack stack) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean hasItem() {
        if (this.slot.hasPower()) {
            return this.getAEFluidStack() != null;
        }
        return false;
    }

    @Override
    public void set(final ItemStack stack) {

    }

    @Override
    public int getMaxStackSize() {
        return 0;
    }

    @Nonnull
    @Override
    public ItemStack remove(final int par1) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean mayPickup(final PlayerEntity player) {
        return false;
    }
}
