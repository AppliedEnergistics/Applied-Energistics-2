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

package appeng.me.cache;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.storage.ItemWatcher;

public class NetworkMonitor<T extends IAEStack<T>> extends MEMonitorHandler<T>
{

	final private GridStorageCache myGridCache;
	final private StorageChannel myChannel;

	boolean sendEvent = false;

	public void forceUpdate()
	{
		this.hasChanged = true;

		Iterator<Entry<IMEMonitorHandlerReceiver<T>, Object>> i = this.getListeners();
		while (i.hasNext())
		{
			Entry<IMEMonitorHandlerReceiver<T>, Object> o = i.next();
			IMEMonitorHandlerReceiver<T> receiver = o.getKey();

			if ( receiver.isValid( o.getValue() ) )
				receiver.onListUpdate();
			else
				i.remove();
		}
	}

	public NetworkMonitor(GridStorageCache cache, StorageChannel chan) {
		super( null, chan );
		this.myGridCache = cache;
		this.myChannel = chan;
	}

	final static public LinkedList DEPTH = new LinkedList();

	@Override
	protected void postChangesToListeners(Iterable<T> changes, BaseActionSource src)
	{
		this.postChange( true, changes, src );
	}

	protected void postChange(boolean Add, Iterable<T> changes, BaseActionSource src)
	{
		if ( DEPTH.contains( this ) )
			return;

		DEPTH.push( this );

		this.sendEvent = true;
		this.notifyListenersOfChange( changes, src );

		IItemList<T> myStorageList = this.getStorageList();

		for (T changedItem : changes)
		{
			T difference = changedItem;

			if ( !Add && changedItem != null )
				(difference = changedItem.copy()).setStackSize( -changedItem.getStackSize() );

			if ( this.myGridCache.interestManager.containsKey( changedItem ) )
			{
				Collection<ItemWatcher> list = this.myGridCache.interestManager.get( changedItem );
				if ( !list.isEmpty() )
				{
					IAEStack fullStack = myStorageList.findPrecise( changedItem );
					if ( fullStack == null )
					{
						fullStack = changedItem.copy();
						fullStack.setStackSize( 0 );
					}

					this.myGridCache.interestManager.enableTransactions();

					for (ItemWatcher iw : list)
						iw.getHost().onStackChange( myStorageList, fullStack, difference, src, this.getChannel() );

					this.myGridCache.interestManager.disableTransactions();
				}
			}
		}

		Object last = DEPTH.pop();
		if ( last != this )
			throw new RuntimeException( "Invalid Access to Networked Storage API detected." );
	}

	public void onTick()
	{
		if ( this.sendEvent )
		{
			this.sendEvent = false;
			this.myGridCache.myGrid.postEvent( new MENetworkStorageEvent( this, this.myChannel ) );
		}
	}

	@Override
	protected IMEInventoryHandler getHandler()
	{
		switch (this.myChannel)
		{
		case ITEMS:
			return this.myGridCache.getItemInventoryHandler();
		case FLUIDS:
			return this.myGridCache.getFluidInventoryHandler();
		default:
		}
		return null;
	}

}
