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

package appeng.blockentity.inventory;

import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.storage.cells.StorageCell;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;

public class AppEngCellInventory extends BaseInternalInventory {
    private final AppEngInternalInventory inv;
    private final StorageCell[] handlerForSlot;
    /**
     * Remembers for which itemstack the handler in handlerForSlot[] was queried.
     */
    private final ItemStack[] handlerStackForSlot;

    public AppEngCellInventory(final InternalInventoryHost host, final int slots) {
        this.inv = new AppEngInternalInventory(host, slots, 1);
        this.handlerForSlot = new StorageCell[slots];
        this.handlerStackForSlot = new ItemStack[slots];
    }

    public void setHandler(int slot, StorageCell handler) {
        this.handlerForSlot[slot] = handler;
        this.handlerStackForSlot[slot] = inv.getStackInSlot(slot);
    }

    public void setFilter(IAEItemFilter filter) {
        this.inv.setFilter(filter);
    }

    @Override
    public void setItemDirect(int slot, ItemStack stack) {
        this.persist(slot);
        this.inv.setItemDirect(slot, stack);
        this.cleanup(slot);
    }

    @Override
    public int size() {
        return this.inv.size();
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
        for (int i = 0; i < this.size(); ++i) {
            this.persist(i);
        }
    }

    private void persist(int slot) {
        if (this.handlerForSlot[slot] != null) {
            this.handlerForSlot[slot].persist();
        }
    }

    private void cleanup(int slot) {
        if (this.handlerForSlot[slot] != null && this.handlerStackForSlot[slot] != this.inv.getStackInSlot(slot)) {
            this.handlerForSlot[slot] = null;
        }
    }
}
