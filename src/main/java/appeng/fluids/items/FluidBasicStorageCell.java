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

package appeng.fluids.items;


import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.items.materials.MaterialType;
import appeng.items.storage.ItemStorageCellBase;


/**
 * @author DrummerMC
 * @version rv6 - 2018-01-17
 * @since rv6 2018-01-17
 */
public final class FluidBasicStorageCell extends ItemStorageCellBase<IAEFluidStack>
{

	private final int perType;
	private final double idleDrain;
	public FluidBasicStorageCell( final MaterialType whichCell, final int kilobytes )
	{
		super(whichCell, kilobytes);
		switch( whichCell )
		{
			case CELL1K_PART:
				this.idleDrain = 0.5;
				this.perType = 8;
				break;
			case CELL4K_PART:
				this.idleDrain = 1.0;
				this.perType = 32;
				break;
			case CELL16K_PART:
				this.idleDrain = 1.5;
				this.perType = 128;
				break;
			case CELL64K_PART:
				this.idleDrain = 2.0;
				this.perType = 512;
				break;
			default:
				this.idleDrain = 0.0;
				this.perType = 8;
		}

	}

	@Override
	public int getBytesPerType( ItemStack cellItem )
	{
		return this.perType;
	}

	@Override
	public double getIdleDrain()
	{
		return this.idleDrain;
	}

	@Override
	public IStorageChannel<IAEFluidStack> getChannel()
	{
		return AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class );
	}

	@Override
	public int getTotalTypes( final ItemStack cellItem )
	{
		return 5;
	}
}