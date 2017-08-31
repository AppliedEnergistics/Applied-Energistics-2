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


import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.ItemList;


class CondenserItemInventory implements IMEMonitor<IAEItemStack>
{

	private final TileCondenser target;

	CondenserItemInventory( final TileCondenser te )
	{
		this.target = te;
	}

	@Override
	public IAEItemStack injectItems( final IAEItemStack input, final Actionable mode, final IActionSource src )
	{
		if( mode == Actionable.MODULATE && input != null )
		{
			this.target.addPower( input.getStackSize() );
		}

		return null;
	}

	@Override
	public IAEItemStack extractItems( final IAEItemStack request, final Actionable mode, final IActionSource src )
	{
		return null;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems( final IItemList out )
	{
		return out;
	}

	@Override
	public IItemList<IAEItemStack> getStorageList()
	{
		return new ItemList();
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public AccessRestriction getAccess()
	{
		return AccessRestriction.WRITE;
	}

	@Override
	public boolean isPrioritized( final IAEItemStack input )
	{
		return false;
	}

	@Override
	public boolean canAccept( final IAEItemStack input )
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
	public void addListener( IMEMonitorHandlerReceiver<IAEItemStack> l, Object verificationToken )
	{
		// Not implemented since the Condenser automatically voids everything, and there are no updates
	}

	@Override
	public void removeListener( IMEMonitorHandlerReceiver<IAEItemStack> l )
	{
		// Not implemented since we don't remember registered listeners anyway
	}
}
