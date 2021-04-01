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

package appeng.client.gui.me.fluids;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.me.common.ReadOnlySlot;

class VirtualFluidSlot extends ReadOnlySlot {

    private final FluidRepo repo;
    private final int offset;

    public VirtualFluidSlot(FluidRepo repo, int offset, int displayX, int displayY) {
        super(displayX, displayY);
        this.repo = repo;
        this.offset = offset;
    }

    public IAEFluidStack getAEFluidStack() {
        if (this.repo.hasPower()) {
            return this.repo.get(this.offset);
        }
        return null;
    }

    @Nonnull
    @Override
    public ItemStack getStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean getHasStack() {
        return this.getAEFluidStack() != null;
    }

}
