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

package appeng.tile.misc;


import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.fluids.util.FluidList;


class CondenserFluidInventory implements IMEMonitor<IAEFluidStack>
{

	private final TileCondenser target;

	CondenserFluidInventory( final TileCondenser te )
	{
		this.target = te;
	}

	@Override
	public IAEFluidStack injectItems( final IAEFluidStack input, final Actionable mode, final IActionSource src )
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
	public IAEFluidStack extractItems( final IAEFluidStack request, final Actionable mode, final IActionSource src )
	{
		return null;
	}

	@Override
	public IItemList<IAEFluidStack> getAvailableItems( final IItemList out )
	{
		return out;
	}

	@Override
	public IItemList<IAEFluidStack> getStorageList()
	{
		return new FluidList();
	}

	@Override
	public IStorageChannel<IAEFluidStack> getChannel()
	{
		return AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class );
	}

	@Override
	public AccessRestriction getAccess()
	{
		return AccessRestriction.WRITE;
	}

	@Override
	public boolean isPrioritized( final IAEFluidStack input )
	{
		return false;
	}

	@Override
	public boolean canAccept( final IAEFluidStack input )
	{
		return true;
	}

	@Override
	public int getPriority()
	{
		return 0;
	}

	@Override
	public int getSlot()
	{
		return 0;
	}

	@Override
	public boolean validForPass( final int i )
	{
		return i == 2;
	}

	@Override
	public void addListener( IMEMonitorHandlerReceiver<IAEFluidStack> l, Object verificationToken )
	{
		// Not implemented since the Condenser automatically voids everything, and there are no updates
	}

	@Override
	public void removeListener( IMEMonitorHandlerReceiver<IAEFluidStack> l )
	{
		// Not implemented since we don't remember registered listeners anyway
	}
}
