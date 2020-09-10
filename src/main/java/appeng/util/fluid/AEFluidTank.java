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

package appeng.util.fluid;

import net.minecraftforge.fluids.capability.templates.FluidTank;

import appeng.api.storage.data.IAEFluidStack;
import appeng.util.Platform;

public class AEFluidTank extends FluidTank implements IAEFluidTank {
    private final IAEFluidInventory host;

    public AEFluidTank(IAEFluidInventory host, int capacity) {
        super(capacity);
        this.host = host;
    }

    @Override
    protected void onContentsChanged() {
        if (this.host != null && Platform.isServer()) {
            this.host.onFluidInventoryChanged(this, 0);
        }
        super.onContentsChanged();
    }

    @Override
    public void setFluidInSlot(int slot, IAEFluidStack fluid) {
        if (slot == 0) {
            this.setFluid(fluid == null ? null : fluid.getFluidStack());
            this.onContentsChanged();
        }
    }

    @Override
    public IAEFluidStack getFluidInSlot(int slot) {
        if (slot == 0) {
            return AEFluidStack.fromFluidStack(this.getFluid());
        }
        return null;
    }

    @Override
    public int getSlots() {
        return 1;
    }

}
