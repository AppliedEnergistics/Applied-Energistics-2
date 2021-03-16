/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.tile.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import appeng.api.storage.cells.ICellInventory;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.filter.IAEItemFilter;

public class AppEngCellInventory implements IItemHandlerModifiable {
    private final AppEngInternalInventory inv;
    private final ICellInventoryHandler handlerForSlot[];

    public AppEngCellInventory(final IAEAppEngInventory host, final int slots) {
        this.inv = new AppEngInternalInventory(host, slots, 1);
        this.handlerForSlot = new ICellInventoryHandler[slots];
    }

    public void setHandler(final int slot, final ICellInventoryHandler handler) {
        this.handlerForSlot[slot] = handler;
    }

    public void setFilter(IAEItemFilter filter) {
        this.inv.setFilter(filter);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        this.persist(slot);
        this.inv.setStackInSlot(slot, stack);
        this.cleanup(slot);
    }

    @Override
    public int getSlots() {
        return this.inv.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        this.persist(slot);
        return this.inv.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        this.persist(slot);
        final ItemStack ret = this.inv.insertItem(slot, stack, simulate);
        this.cleanup(slot);
        return ret;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        this.persist(slot);
        final ItemStack ret = this.inv.extractItem(slot, amount, simulate);
        this.cleanup(slot);
        return ret;
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.inv.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.inv.isItemValid(slot, stack);
    }

    public void persist() {
        for (int i = 0; i < this.getSlots(); ++i) {
            this.persist(i);
        }
    }

    private void persist(int slot) {
        if (this.handlerForSlot[slot] != null) {
            final ICellInventory ci = this.handlerForSlot[slot].getCellInv();
            if (ci != null) {
                ci.persist();
            }
        }
    }

    private void cleanup(int slot) {
        if (this.handlerForSlot[slot] != null) {
            final ICellInventory ci = this.handlerForSlot[slot].getCellInv();

            if (ci == null || ci.getItemStack() != this.inv.getStackInSlot(slot)) {
                this.handlerForSlot[slot] = null;
            }
        }
    }
}
