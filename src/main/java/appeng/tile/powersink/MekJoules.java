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

package appeng.tile.powersink;


import net.minecraftforge.common.util.ForgeDirection;

import mekanism.api.energy.IStrictEnergyAcceptor;

import appeng.api.config.PowerUnits;
import appeng.transformer.annotations.Integration.Interface;


@Interface( iname = "Mekanism", iface = "mekanism.api.energy.IStrictEnergyAcceptor" )
public abstract class MekJoules extends RedstoneFlux implements IStrictEnergyAcceptor
{

	@Override
	public double getEnergy()
	{
		return 0;
	}

	@Override
	public void setEnergy( double energy )
	{
		double extra = this.injectExternalPower( PowerUnits.MK, energy );
		this.internalCurrentPower += PowerUnits.MK.convertTo( PowerUnits.AE, extra );
	}

	@Override
	public double getMaxEnergy()
	{
		return this.getExternalPowerDemand( PowerUnits.MK, 100000 );
	}

	@Override
	public double transferEnergyToAcceptor( ForgeDirection side, double amount )
	{
		double demand = this.getExternalPowerDemand( PowerUnits.MK, Double.MAX_VALUE );
		if( amount > demand )
			amount = demand;

		double overflow = this.injectExternalPower( PowerUnits.MK, amount );
		return amount - overflow;
	}

	@Override
	public boolean canReceiveEnergy( ForgeDirection side )
	{
		return this.getPowerSides().contains( side );
	}
}
