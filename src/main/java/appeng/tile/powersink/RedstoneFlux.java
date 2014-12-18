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

import cofh.api.energy.IEnergyReceiver;

import appeng.api.config.PowerUnits;
import appeng.transformer.annotations.integration.Interface;


@Interface( iname = "RF", iface = "cofh.api.energy.IEnergyReceiver" )
public abstract class RedstoneFlux extends RotaryCraft implements IEnergyReceiver
{
	@Override
	final public int receiveEnergy( ForgeDirection from, int maxReceive, boolean simulate )
	{
		final int networkRFDemand = ( int ) Math.floor( this.getExternalPowerDemand( PowerUnits.RF, maxReceive ) );
		final int usedRF = Math.min( maxReceive, networkRFDemand );

		if ( !simulate )
		{
			this.injectExternalPower( PowerUnits.RF, usedRF );
		}

		return usedRF;
	}

	@Override
	final public int getEnergyStored( ForgeDirection from )
	{
		return ( int ) Math.floor( PowerUnits.AE.convertTo( PowerUnits.RF, this.getAECurrentPower() ) );
	}

	@Override
	final public int getMaxEnergyStored( ForgeDirection from )
	{
		return ( int ) Math.floor( PowerUnits.AE.convertTo( PowerUnits.RF, this.getAEMaxPower() ) );
	}

	@Override
	final public boolean canConnectEnergy( ForgeDirection from )
	{
		return this.getPowerSides().contains( from );
	}
}
