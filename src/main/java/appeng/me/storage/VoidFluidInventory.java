/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.me.storage;


import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.tile.misc.TileCondenser;


public final class VoidFluidInventory implements IMEInventoryHandler<IAEFluidStack>
{

	final TileCondenser target;

	public VoidFluidInventory( TileCondenser te )
	{
		this.target = te;
	}

	@Override
	public final IAEFluidStack injectItems( IAEFluidStack input, Actionable mode, BaseActionSource src )
	{
		if( mode == Actionable.SIMULATE )
		{
			return null;
		}

		if( input != null )
		{
			this.target.addPower( input.getStackSize() / 1000.0 );
		}
		return null;
	}

	@Override
	public final IAEFluidStack extractItems( IAEFluidStack request, Actionable mode, BaseActionSource src )
	{
		return null;
	}

	@Override
	public final IItemList<IAEFluidStack> getAvailableItems( IItemList out )
	{
		return out;
	}

	@Override
	public final StorageChannel getChannel()
	{
		return StorageChannel.FLUIDS;
	}

	@Override
	public final AccessRestriction getAccess()
	{
		return AccessRestriction.WRITE;
	}

	@Override
	public final boolean isPrioritized( IAEFluidStack input )
	{
		return false;
	}

	@Override
	public final boolean canAccept( IAEFluidStack input )
	{
		return true;
	}

	@Override
	public final int getPriority()
	{
		return 0;
	}

	@Override
	public final int getSlot()
	{
		return 0;
	}

	@Override
	public final boolean validForPass( int i )
	{
		return i == 2;
	}
}
