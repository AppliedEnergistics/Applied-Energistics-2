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

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;
import alexiil.mc.lib.attributes.item.impl.DelegatingFixedItemInv;

import appeng.api.storage.cells.ICellInventory;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.core.Api;
import appeng.util.inv.IAEAppEngInventory;

public class AppEngCellInventory extends DelegatingFixedItemInv {
    private static final ItemFilter CELL_FILTER = stack -> !stack.isEmpty()
            && Api.instance().registries().cell().isCellHandled(stack);

    private final ICellInventoryHandler<?>[] handlerForSlot;

    public AppEngCellInventory(final IAEAppEngInventory host, final int slots) {
        super(new AppEngInternalInventory(host, slots, 1));
        this.handlerForSlot = new ICellInventoryHandler[slots];
    }

    @Override
    public ItemFilter getFilterForSlot(int slot) {
        return CELL_FILTER;
    }

    public void setHandler(final int slot, final ICellInventoryHandler<?> handler) {
        this.handlerForSlot[slot] = handler;
    }

    @Override
    public boolean setInvStack(int slot, ItemStack to, Simulation simulation) {
        this.persist(slot);
        boolean result = super.setInvStack(slot, to, simulation);
        this.cleanup(slot);
        return result;
    }

    @Override
    public ItemStack getInvStack(int slot) {
        this.persist(slot);
        return super.getInvStack(slot);
    }

    private void persist(int slot) {
        if (this.handlerForSlot[slot] != null) {
            final ICellInventory<?> ci = this.handlerForSlot[slot].getCellInv();
            if (ci != null) {
                ci.persist();
            }
        }
    }

    private void cleanup(int slot) {
        if (this.handlerForSlot[slot] != null) {
            final ICellInventory<?> ci = this.handlerForSlot[slot].getCellInv();

            if (ci == null || ci.getItemStack() != super.getInvStack(slot)) {
                this.handlerForSlot[slot] = null;
            }
        }
    }
}
