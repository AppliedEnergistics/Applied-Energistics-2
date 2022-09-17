/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.me.helpers;


import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;


public class ChannelPowerSrc implements IEnergySource {

    private final IGridNode node;
    private final IEnergySource realSrc;

    public ChannelPowerSrc(final IGridNode networkNode, final IEnergySource src) {
        this.node = networkNode;
        this.realSrc = src;
    }

    @Override
    public double extractAEPower(final double amt, final Actionable mode, final PowerMultiplier usePowerMultiplier) {
        if (this.node.isActive()) {
            return this.realSrc.extractAEPower(amt, mode, usePowerMultiplier);
        }
        return 0.0;
    }
}
