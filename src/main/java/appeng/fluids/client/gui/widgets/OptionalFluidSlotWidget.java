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

package appeng.fluids.client.gui.widgets;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.Point;
import appeng.container.slot.IOptionalSlot;
import appeng.container.slot.IOptionalSlotHost;
import appeng.fluids.util.IAEFluidTank;

public class OptionalFluidSlotWidget extends FluidSlotWidget implements IOptionalSlot {
    private final IOptionalSlotHost containerBus;
    private final int groupNum;

    public OptionalFluidSlotWidget(IAEFluidTank fluids, final IOptionalSlotHost containerBus, int slot, int groupNum) {
        super(fluids, slot);
        this.containerBus = containerBus;
        this.groupNum = groupNum;
    }

    @Override
    public boolean isSlotEnabled() {
        if (this.containerBus == null) {
            return false;
        }
        return this.containerBus.isSlotEnabled(this.groupNum);
    }

    @Override
    public IAEFluidStack getFluidStack() {
        if (!this.isSlotEnabled() && super.getFluidStack() != null) {
            this.setFluidStack(null);
        }
        return super.getFluidStack();
    }

    @Override
    public Point getBackgroundPos() {
        return new Point(getTooltipAreaX() - 1, getTooltipAreaY() - 1);
    }
}
