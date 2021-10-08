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

package appeng.util.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import appeng.api.storage.data.IAEFluidStack;

public interface IAEFluidTank extends Storage<FluidVariant> {
    void setFluidInSlot(final int slot, final IAEFluidStack fluid);

    IAEFluidStack getFluidInSlot(final int slot);

    int getSlots();

    long getTankCapacity(int tankIndex);

    long fill(int slot, IAEFluidStack stack, boolean doFill);

    long drain(int slot, IAEFluidStack stack, boolean doDrain);
}
