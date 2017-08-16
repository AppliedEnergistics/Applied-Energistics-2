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

package appeng.items.tools.powered.powersink;


import net.minecraft.item.ItemStack;

import cofh.redstoneflux.api.IEnergyContainerItem;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import appeng.coremod.annotations.Integration.Interface;
import appeng.integration.IntegrationType;


@Interface( iface = "cofh.redstoneflux.api.IEnergyContainerItem", iname = IntegrationType.RFItem )
public abstract class RedstoneFlux extends AEBasePoweredItem implements IEnergyContainerItem
{
	public RedstoneFlux( final double powerCapacity )
	{
		super( powerCapacity );
	}

	@Override
	public int receiveEnergy( final ItemStack is, final int maxReceive, final boolean simulate )
	{
		final double convertedPower = PowerUnits.RF.convertTo( PowerUnits.AE, maxReceive );
		final double overflow = (int) this.injectAEPower( is, convertedPower, simulate ? Actionable.SIMULATE : Actionable.MODULATE );

		return (int) ( maxReceive - overflow );
	}

	@Override
	public int extractEnergy( final ItemStack container, final int maxExtract, final boolean simulate )
	{
		return 0;
	}

	@Override
	public int getEnergyStored( final ItemStack is )
	{
		return (int) PowerUnits.AE.convertTo( PowerUnits.RF, this.getAECurrentPower( is ) );
	}

	@Override
	public int getMaxEnergyStored( final ItemStack is )
	{
		return (int) PowerUnits.AE.convertTo( PowerUnits.RF, this.getAEMaxPower( is ) );
	}
}
