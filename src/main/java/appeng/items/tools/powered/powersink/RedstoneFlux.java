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


import appeng.api.config.PowerUnits;
import appeng.integration.IntegrationType;
import appeng.transformer.annotations.Integration.Interface;
import cofh.api.energy.IEnergyContainerItem;
import com.google.common.base.Optional;
import net.minecraft.item.ItemStack;


@Interface( iface = "cofh.api.energy.IEnergyContainerItem", iname = IntegrationType.RFItem )
public abstract class RedstoneFlux extends IC2 implements IEnergyContainerItem
{
	public RedstoneFlux( final double powerCapacity, final Optional<String> subName )
	{
		super( powerCapacity, subName );
	}

	@Override
	public int receiveEnergy( final ItemStack is, final int maxReceive, final boolean simulate )
	{
		return maxReceive - (int) this.injectExternalPower( PowerUnits.RF, is, maxReceive, simulate );
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
