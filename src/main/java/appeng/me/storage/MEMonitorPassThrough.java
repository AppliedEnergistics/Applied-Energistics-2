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

package appeng.me.storage;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import appeng.util.inv.ItemListIgnoreCrafting;


public class MEMonitorPassThrough<T extends IAEStack<T>> extends MEPassThrough<T> implements IMEMonitor<T>, IMEMonitorHandlerReceiver<T>
{

	final HashMap<IMEMonitorHandlerReceiver<T>, Object> listeners = new HashMap<IMEMonitorHandlerReceiver<T>, Object>();
	public BaseActionSource changeSource;
	IMEMonitor<T> monitor;

	public MEMonitorPassThrough( IMEInventory<T> i, StorageChannel channel )
	{
		super( i, channel );
		if( i instanceof IMEMonitor )
		{
			this.monitor = (IMEMonitor<T>) i;
		}
	}

	@Override
	public void setInternal( IMEInventory<T> i )
	{
		if( this.monitor != null )
		{
			this.monitor.removeListener( this );
		}

		this.monitor = null;
		IItemList<T> before = this.getInternal() == null ? this.channel.createList() : this.getInternal().getAvailableItems( new ItemListIgnoreCrafting( this.channel.createList() ) );

		super.setInternal( i );
		if( i instanceof IMEMonitor )
		{
			this.monitor = (IMEMonitor<T>) i;
		}

		IItemList<T> after = this.getInternal() == null ? this.channel.createList() : this.getInternal().getAvailableItems( new ItemListIgnoreCrafting( this.channel.createList() ) );

		if( this.monitor != null )
		{
			this.monitor.addListener( this, this.monitor );
		}

		Platform.postListChanges( before, after, this, this.changeSource );
	}

	@Override
	public IItemList<T> getAvailableItems( IItemList out )
	{
		super.getAvailableItems( new ItemListIgnoreCrafting( out ) );
		return out;
	}

	@Override
	public void addListener( IMEMonitorHandlerReceiver<T> l, Object verificationToken )
	{
		this.listeners.put( l, verificationToken );
	}

	@Override
	public void removeListener( IMEMonitorHandlerReceiver<T> l )
	{
		this.listeners.remove( l );
	}

	@Override
	public IItemList<T> getStorageList()
	{
		if( this.monitor == null )
		{
			IItemList<T> out = this.channel.createList();
			this.getInternal().getAvailableItems( new ItemListIgnoreCrafting( out ) );
			return out;
		}
		return this.monitor.getStorageList();
	}

	@Override
	public boolean isValid( Object verificationToken )
	{
		return verificationToken == this.monitor;
	}

	@Override
	public void postChange( IBaseMonitor<T> monitor, Iterable<T> change, BaseActionSource source )
	{
		Iterator<Entry<IMEMonitorHandlerReceiver<T>, Object>> i = this.listeners.entrySet().iterator();
		while( i.hasNext() )
		{
			Entry<IMEMonitorHandlerReceiver<T>, Object> e = i.next();
			IMEMonitorHandlerReceiver<T> receiver = e.getKey();
			if( receiver.isValid( e.getValue() ) )
			{
				receiver.postChange( this, change, source );
			}
			else
			{
				i.remove();
			}
		}
	}

	@Override
	public void onListUpdate()
	{
		Iterator<Entry<IMEMonitorHandlerReceiver<T>, Object>> i = this.listeners.entrySet().iterator();
		while( i.hasNext() )
		{
			Entry<IMEMonitorHandlerReceiver<T>, Object> e = i.next();
			IMEMonitorHandlerReceiver<T> receiver = e.getKey();
			if( receiver.isValid( e.getValue() ) )
			{
				receiver.onListUpdate();
			}
			else
			{
				i.remove();
			}
		}
	}
}
