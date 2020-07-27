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
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import alexiil.mc.lib.attributes.ListenerRemovalToken;
import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.InvMarkDirtyListener;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;

import appeng.util.inv.filter.IAEItemFilter;

// FIXME: Needs to be double checked, LBA has better ways of doing this
public class WrapperFilteredItemHandler implements FixedItemInv {
    private final FixedItemInv handler;
    private final IAEItemFilter filter;

    public WrapperFilteredItemHandler(@Nonnull FixedItemInv handler, @Nonnull IAEItemFilter filter) {
        this.handler = handler;
        this.filter = filter;
    }

    @Override
    public ItemStack getInvStack(int slot) {
        return this.handler.getInvStack(slot);
    }

    @Override
    public boolean setInvStack(int slot, ItemStack to, Simulation simulation) {
        return this.handler.setInvStack(slot, to, simulation);
    }

    @Override
    public int getSlotCount() {
        return this.handler.getSlotCount();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return filter.allowInsert(handler, slot, stack);
    }

    @Override
    public ItemFilter getFilterForSlot(int slot) {
        return stack -> filter.allowInsert(handler, slot, stack);
    }

    @Override
    public int getChangeValue() {
        return this.handler.getChangeValue();
    }

    @Nullable
    @Override
    public ListenerToken addListener(InvMarkDirtyListener listener, ListenerRemovalToken removalToken) {
        return this.handler.addListener(listener, removalToken);
    }

}
