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


import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.storage.ItemWatcher;
import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map.Entry;


public class NetworkMonitor<T extends IAEStack<T>> implements IMEMonitor<T>
{
	@Nonnull
	private static final Deque<NetworkMonitor<?>> GLOBAL_DEPTH = Queues.newArrayDeque();

	@Nonnull
	private final GridStorageCache myGridCache;
	@Nonnull
	private final IStorageChannel<T> myChannel;
	@Nonnull
	private final IItemList<T> cachedList;
	@Nonnull
	private final Object2ObjectMap<IMEMonitorHandlerReceiver<T>, Object> listeners;

	private boolean sendEvent = false;
	private boolean forceUpdate = false;
	@Nonnegative
	private int localDepthSemaphore = 0;
	private long gridItemCount;
	private long gridFluidCount;

	public NetworkMonitor( final GridStorageCache cache, final IStorageChannel<T> chan )
	{
		this.myGridCache = cache;
		this.myChannel = chan;
		this.cachedList = chan.createList();
		this.listeners = new Object2ObjectOpenHashMap<>();
	}

	@Override
	public void addListener( final IMEMonitorHandlerReceiver<T> l, final Object verificationToken )
	{
		this.listeners.put( l, verificationToken );
	}

	@Override
	public boolean canAccept( final T input )
	{
		return this.getHandler().canAccept( input );
	}

	@Override
	public T extractItems( final T request, final Actionable mode, final IActionSource src )
	{
		return this.getHandler().extractItems( request, mode, src );
	}

	@Override
	public AccessRestriction getAccess()
	{
		return this.getHandler().getAccess();
	}

	@Override
	public IItemList<T> getAvailableItems( final IItemList<T> out )
	{
		return this.getHandler().getAvailableItems( out );
	}

	@Override
	public IStorageChannel<T> getChannel()
	{
		return this.getHandler().getChannel();
	}

	@Override
	public int getPriority()
	{
		return this.getHandler().getPriority();
	}

	@Override
	public int getSlot()
	{
		return this.getHandler().getSlot();
	}

	public long getGridCurrentCount()
	{
		if( myChannel == AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) )
		{
			return gridItemCount;
		}
		else if( myChannel == AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) )
		{
			return gridFluidCount;
		}
		return 0;
	}

	public long incGridCurrentCount(long count)
	{
		if( myChannel == AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) )
		{
			return gridItemCount += count;
		}
		else if( myChannel == AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) )
		{
			return gridFluidCount += count;
		}
		return 0;
	}

	@Nonnull
	@Override
	public IItemList<T> getStorageList()
	{
		if( forceUpdate )
		{
			forceUpdate = false;
			this.cachedList.resetStatus();
			return this.getAvailableItems( this.cachedList );
		}

		return this.cachedList;
	}

	@Override
	public T injectItems( final T input, final Actionable mode, final IActionSource src )
	{
		return this.getHandler().injectItems( input, mode, src );
	}

	@Override
	public boolean isPrioritized( final T input )
	{
		return this.getHandler().isPrioritized( input );
	}

	@Override
	public void removeListener( final IMEMonitorHandlerReceiver<T> l )
	{
		this.listeners.remove( l );
	}

	@Override
	public boolean validForPass( final int i )
	{
		return this.getHandler().validForPass( i );
	}

	@Nullable
	private IMEInventoryHandler<T> getHandler()
	{
		return this.myGridCache.getInventoryHandler( this.myChannel );
	}

	private Iterator<Entry<IMEMonitorHandlerReceiver<T>, Object>> getListeners()
	{
		return this.listeners.entrySet().iterator();
	}

	private void notifyListenersOfChange( final Iterable<T> diff, final IActionSource src )
	{
		final Iterator<Entry<IMEMonitorHandlerReceiver<T>, Object>> i = this.getListeners();

		while( i.hasNext() )
		{
			final Entry<IMEMonitorHandlerReceiver<T>, Object> o = i.next();
			final IMEMonitorHandlerReceiver<T> receiver = o.getKey();
			if( receiver.isValid( o.getValue() ) )
			{
				receiver.postChange( this, diff, src );
			}
			else
			{
				i.remove();
			}
		}
	}

	protected void updateCraftables( Iterable<T> input, IActionSource src )
	{
		for( final T changedItem : input )
		{
			if (changedItem.isCraftable()) {
				this.cachedList.add( changedItem );
			}
			else
			{
				T i = this.cachedList.findPrecise( changedItem );
				if (i != null)
				i.setCraftable( false );
			}
		}
	}

	protected void postChange( final boolean add, final Iterable<T> changes, final IActionSource src )
	{
		if( this.localDepthSemaphore > 0 || GLOBAL_DEPTH.contains( this ) )
		{
			return;
		}

		this.localDepthSemaphore++;
		GLOBAL_DEPTH.push( this );

		this.sendEvent = true;

		for( final T changed : changes )
		{
			T change = changed;
			if( !add && change != null )
			{
				change = changed.copy();
				change.setStackSize( -change.getStackSize() );
			}

			incGridCurrentCount( change.getStackSize() );
			this.cachedList.add( change );

			if( this.myGridCache.getInterestManager().containsKey( change ) )
			{
				final Collection<ItemWatcher> list = this.myGridCache.getInterestManager().get( change );

				if( !list.isEmpty() )
				{
					IAEStack<T> fullStack = this.getStorageList().findPrecise( change );

					if( fullStack == null )
					{
						fullStack = change.copy();
						fullStack.setStackSize( 0 );
					}

					this.myGridCache.getInterestManager().enableTransactions();

					for( final ItemWatcher iw : list )
					{
						iw.getHost().onStackChange( this.getStorageList(), fullStack, change, src, this.getChannel() );
					}

					this.myGridCache.getInterestManager().disableTransactions();
				}
			}
		}

		this.notifyListenersOfChange( changes, src );

		final NetworkMonitor<?> last = GLOBAL_DEPTH.pop();
		this.localDepthSemaphore--;

		if( last != this )
		{
			throw new IllegalStateException( "Invalid Access to Networked Storage API detected." );
		}
	}

	void forceUpdate()
	{
		this.forceUpdate = true;

		final Iterator<Entry<IMEMonitorHandlerReceiver<T>, Object>> i = this.getListeners();
		while ( i.hasNext() )
		{
			final Entry<IMEMonitorHandlerReceiver<T>, Object> o = i.next();
			final IMEMonitorHandlerReceiver<T> receiver = o.getKey();

			if( receiver.isValid( o.getValue() ) )
			{
				receiver.onListUpdate();
			}
			else
			{
				i.remove();
			}
		}
	}

	void onTick()
	{
		if( this.sendEvent )
		{
			this.sendEvent = false;
			this.myGridCache.getGrid().postEvent( new MENetworkStorageEvent( this, this.myChannel ) );
		}

	}
}
