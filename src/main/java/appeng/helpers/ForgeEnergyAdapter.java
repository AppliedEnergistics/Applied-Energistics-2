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

package appeng.helpers;

import net.minecraftforge.energy.IEnergyStorage;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import appeng.tile.powersink.IExternalPowerSink;

import java.util.function.DoubleSupplier;

/**
 * Adapts an {@link IExternalPowerSink} to Forges {@link IEnergyStorage}.
 */
public class ForgeEnergyAdapter implements IEnergyStorage {

    private final IExternalPowerSink sink;

    public ForgeEnergyAdapter(IExternalPowerSink sink) {
        this.sink = sink;
    }

    @Override
    public final int receiveEnergy(int maxReceive, boolean simulate) {
        final double offered = maxReceive;
        final double overflow = this.sink.injectExternalPower(PowerUnits.RF, offered,
                simulate ? Actionable.SIMULATE : Actionable.MODULATE);

        return (int) (maxReceive - overflow);
    }

    @Override
    public final int getEnergyStored() {
        return (int) Math.floor(PowerUnits.AE.convertTo(PowerUnits.RF, this.sink.getAECurrentPower()));
    }

    @Override
    public final int getMaxEnergyStored() {
        return (int) Math.floor(PowerUnits.AE.convertTo(PowerUnits.RF, this.sink.getAEMaxPower()));
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

}
