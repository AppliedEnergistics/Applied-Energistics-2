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

package appeng.integration.modules.ic2;


import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import appeng.integration.abstraction.IC2PowerSink;
import appeng.tile.powersink.IExternalPowerSink;
import ic2.api.energy.prefab.BasicSink;
import ic2.api.energy.tile.IEnergyEmitter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.util.EnumSet;
import java.util.Set;


/**
 * The real implementation of IC2PowerSink.
 */
public class IC2PowerSinkAdapter extends BasicSink implements IC2PowerSink {

    private final IExternalPowerSink powerSink;

    private final Set<EnumFacing> validFaces = EnumSet.allOf(EnumFacing.class);

    public IC2PowerSinkAdapter(TileEntity tileEntity, IExternalPowerSink powerSink) {
        super(tileEntity, 0, Integer.MAX_VALUE);
        this.powerSink = powerSink;
    }

    @Override
    public void invalidate() {
        super.onChunkUnload();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public double getDemandedEnergy() {
        return this.powerSink.getExternalPowerDemand(PowerUnits.EU, Double.MAX_VALUE);
    }

    @Override
    public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
        return PowerUnits.EU.convertTo(PowerUnits.AE, this.powerSink.injectExternalPower(PowerUnits.EU, amount, Actionable.MODULATE));
    }

    @Override
    public boolean acceptsEnergyFrom(IEnergyEmitter iEnergyEmitter, EnumFacing side) {
        return this.validFaces.contains(side);
    }

    @Override
    public void setValidFaces(Set<EnumFacing> faces) {
        this.validFaces.clear();
        this.validFaces.addAll(faces);
    }
}
