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

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import alexiil.mc.lib.attributes.ListenerRemovalToken;
import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.GroupedItemInv;
import alexiil.mc.lib.attributes.item.InvMarkDirtyListener;
import alexiil.mc.lib.attributes.item.impl.DelegatingGroupedItemInv;

public class WrapperSupplierItemHandler implements FixedItemInv {
    private final Supplier<FixedItemInv> sourceHandler;

    public WrapperSupplierItemHandler(Supplier<FixedItemInv> source) {
        this.sourceHandler = source;
    }

    @Override
    public GroupedItemInv getGroupedInv() {
        return new DelegatingGroupedItemInv(this.sourceHandler.get().getGroupedInv());
    }

    @Override
    public ItemStack getInvStack(int slot) {
        return this.sourceHandler.get().getInvStack(slot);
    }

    @Override
    public boolean setInvStack(int slot, ItemStack to, Simulation simulation) {
        return this.sourceHandler.get().setInvStack(slot, to, simulation);
    }

    @Override
    public int getSlotCount() {
        return this.sourceHandler.get().getSlotCount();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return this.sourceHandler.get().isItemValidForSlot(slot, stack);
    }

    @Override
    public int getChangeValue() {
        return this.sourceHandler.get().getChangeValue();
    }

    @Nullable
    @Override
    public ListenerToken addListener(InvMarkDirtyListener listener, ListenerRemovalToken removalToken) {
        return this.sourceHandler.get().addListener(listener, removalToken);
    }

}
