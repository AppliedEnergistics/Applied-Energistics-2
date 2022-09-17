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

package appeng.client.me;


import appeng.api.storage.data.IAEFluidStack;


/**
 * @author BrockWS
 * @version rv6 - 22/05/2018
 * @since rv6 22/05/2018
 */
public class InternalFluidSlotME {

    private final int offset;
    private final int xPos;
    private final int yPos;
    private final FluidRepo repo;

    public InternalFluidSlotME(final FluidRepo def, final int offset, final int displayX, final int displayY) {
        this.repo = def;
        this.offset = offset;
        this.xPos = displayX;
        this.yPos = displayY;
    }

    IAEFluidStack getAEStack() {
        return this.repo.getReferenceFluid(this.offset);
    }

    boolean hasPower() {
        return this.repo.hasPower();
    }

    int getxPosition() {
        return this.xPos;
    }

    int getyPosition() {
        return this.yPos;
    }
}
