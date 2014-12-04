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
import appeng.api.config.PowerUnits;
import appeng.transformer.annotations.integration.Interface;
import cofh.api.energy.IEnergyHandler;

@Interface(iname = "RF", iface = "cofh.api.energy.IEnergyHandler")
public abstract class RedstoneFlux extends RotaryCraft implements IEnergyHandler
{
	@Override
	final public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if ( simulate )
		{
			final int networkEnergyDemand = (int) Math.floor( this.getExternalPowerDemand( PowerUnits.RF, maxReceive ) );
			final int maxEnergyStorage = this.getMaxEnergyStored( from );
			final int currentEnergyStorage = this.getEnergyStored( from );
			final int energyStorageBalance = maxEnergyStorage - currentEnergyStorage + networkEnergyDemand - maxReceive;

			return Math.max( 0, energyStorageBalance );
		}
		else
		{
			int demand = (int) Math.floor( this.getExternalPowerDemand( PowerUnits.RF, maxReceive ) );

			int ignored = 0;
			int insertAmt = maxReceive;

			if ( insertAmt > demand )
			{
				ignored = insertAmt - demand;
				insertAmt = demand;
			}

			double overFlow = this.injectExternalPower( PowerUnits.RF, insertAmt );
			double ox = Math.floor( overFlow );
			this.internalCurrentPower += PowerUnits.RF.convertTo( PowerUnits.AE, overFlow - ox );
			return maxReceive - ((int) ox + ignored);
		}
	}

	@Override
	final public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		return 0;
	}

	@Override
	final public boolean canConnectEnergy(ForgeDirection from)
	{
		return this.getPowerSides().contains( from );
	}

	@Override
	final public int getEnergyStored(ForgeDirection from)
	{
		return (int) Math.floor( PowerUnits.AE.convertTo( PowerUnits.RF, this.getAECurrentPower() ) );
	}

	@Override
	final public int getMaxEnergyStored(ForgeDirection from)
	{
		return (int) Math.floor( PowerUnits.AE.convertTo( PowerUnits.RF, this.getAEMaxPower() ) );
	}
}
