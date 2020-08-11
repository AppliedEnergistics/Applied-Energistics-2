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

import net.minecraft.item.ItemStack;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.ItemStackUtil;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;
import alexiil.mc.lib.attributes.item.impl.DelegatingFixedItemInv;

import appeng.util.inv.filter.IAEItemFilter;

// FIXME: Needs to be double checked, LBA has better ways of doing this
public class WrapperFilteredItemHandler extends DelegatingFixedItemInv {
    private final IAEItemFilter filter;

    public WrapperFilteredItemHandler(@Nonnull FixedItemInv handler, @Nonnull IAEItemFilter filter) {
        super(handler);
        this.filter = filter;
    }

    @Override
    public boolean setInvStack(int slot, ItemStack to, Simulation simulation) {
        ItemStack current = this.getInvStack(slot);
        boolean same = ItemStackUtil.areEqualIgnoreAmounts(current, to);
        boolean isExtracting = !current.isEmpty() && (!same || to.getCount() < current.getCount());
        boolean isInserting = !to.isEmpty() && (!same || to.getCount() > current.getCount());

        if (isExtracting) {
            // We may be extracting by "exchanging" the current item for something else,
            // or we might be setting it to air
            int extractAmount = current.getCount();
            if (same && !to.isEmpty()) {
                extractAmount -= to.getCount();
            }

            if (!filter.allowExtract(delegate, slot, extractAmount)) {
                return false;
            }
        }

        if (isInserting) {
            if (!filter.allowInsert(delegate, slot, to)) {
                return false;
            }
        }

        return super.setInvStack(slot, to, simulation);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return (stack.isEmpty() || this.getFilterForSlot(slot).matches(stack)) && super.isItemValidForSlot(slot, stack);
    }

    @Override
    public ItemFilter getFilterForSlot(int slot) {
        ItemFilter parentFilter = super.getFilterForSlot(slot);
        ItemFilter thisFilter = stack -> filter.allowInsert(delegate, slot, stack);
        return thisFilter.and(parentFilter);
    }

}
