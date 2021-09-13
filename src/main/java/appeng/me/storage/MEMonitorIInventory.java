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


import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.StorageFilter;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.ItemSlot;


public class MEMonitorIInventory implements IMEMonitor<IAEItemStack>, ITickingMonitor
{

	private final InventoryAdaptor adaptor;
	private IItemList<IAEItemStack> cache = AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createList();

	private final HashMap<IMEMonitorHandlerReceiver<IAEItemStack>, Object> listeners = new HashMap<>();
	private IActionSource mySource;
	private StorageFilter mode = StorageFilter.EXTRACTABLE_ONLY;

	public MEMonitorIInventory( final InventoryAdaptor adaptor )
	{
		this.adaptor = adaptor;
	}

	@Override
	public void addListener( final IMEMonitorHandlerReceiver<IAEItemStack> l, final Object verificationToken )
	{
		this.listeners.put( l, verificationToken );
	}

	@Override
	public void removeListener( final IMEMonitorHandlerReceiver<IAEItemStack> l )
	{
		this.listeners.remove( l );
	}

	@Override
	public IAEItemStack injectItems( final IAEItemStack input, final Actionable type, final IActionSource src )
	{
		ItemStack out = ItemStack.EMPTY;

		if( type == Actionable.SIMULATE )
		{
			out = this.adaptor.simulateAdd( input.createItemStack() );
		}
		else
		{
			out = this.adaptor.addItems( input.createItemStack() );
		}

		if( out.isEmpty() )
		{
			return null;
		}

		// better then doing construction from scratch :3
		final IAEItemStack o = input.copy();
		o.setStackSize( out.getCount() );

		if( type == Actionable.MODULATE )
		{
			IAEItemStack added = o.copy();
			this.cache.add( added );
			this.postDifference( Collections.singletonList( added ) );
			this.onTick();
		}

		return o;
	}

	@Override
	public IAEItemStack extractItems( final IAEItemStack request, final Actionable type, final IActionSource src )
	{
		ItemStack out = ItemStack.EMPTY;

		if( type == Actionable.SIMULATE )
		{
			out = this.adaptor.simulateRemove( (int) request.getStackSize(), request.getDefinition(), null );
		}
		else
		{
			out = this.adaptor.removeItems( (int) request.getStackSize(), request.getDefinition(), null );
		}

		if( out.isEmpty() )
		{
			return null;
		}

		// better then doing construction from scratch :3
		final IAEItemStack o = request.copy();
		o.setStackSize( out.getCount() );

		if( type == Actionable.MODULATE )
		{
			IAEItemStack cachedStack = this.cache.findPrecise( request );
			if( cachedStack != null )
			{
				cachedStack.decStackSize( o.getStackSize() );
				this.postDifference( Collections.singletonList( o.copy().setStackSize( -o.getStackSize() ) ) );
			}
			this.onTick();
		}

		return o;
	}

	@Override
	public IStorageChannel getChannel()
	{
		return AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class );
	}

	@Override
	public TickRateModulation onTick()
	{
		boolean changed = false;

		final List<IAEItemStack> changes = new ArrayList<>();

		IItemList<IAEItemStack> currentlyOnStorage = AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createList();

		for( final ItemSlot is : adaptor )
		{
			if( this.mode == StorageFilter.EXTRACTABLE_ONLY && !is.isExtractable() )
			{
				continue;
			}
			currentlyOnStorage.add( is.getAEItemStack() );
		}

		for( final IAEItemStack is : cache )
		{
			is.setStackSize( -is.getStackSize() );
		}

		for( final IAEItemStack is : currentlyOnStorage )
		{
			cache.add( is );
		}

		for( final IAEItemStack is : cache )
		{
			if( is.getStackSize() != 0 )
			{
				changes.add( is );
			}
		}

		cache = currentlyOnStorage;

		if( !changes.isEmpty() )
		{
			this.postDifference( changes );
			changed = true;
		}

		return changed ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
	}

	private void postDifference( final Iterable<IAEItemStack> a )
	{
		if( a != null )
		{
			final Iterator<Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object>> i = this.listeners.entrySet().iterator();
			while( i.hasNext() )
			{
				final Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object> l = i.next();
				final IMEMonitorHandlerReceiver<IAEItemStack> key = l.getKey();
				if( key.isValid( l.getValue() ) )
				{
					key.postChange( this, a, this.getActionSource() );
				}
				else
				{
					i.remove();
				}
			}
		}
	}

	@Override
	public AccessRestriction getAccess()
	{
		return AccessRestriction.READ_WRITE;
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
		return true;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems( final IItemList out )
	{
		for( IAEItemStack is : cache )
		{
			out.addStorage( is );
		}

		return out;
	}

	@Override
	public IItemList<IAEItemStack> getStorageList()
	{
		return this.cache;
	}

	private StorageFilter getMode()
	{
		return this.mode;
	}

	public void setMode( final StorageFilter mode )
	{
		this.mode = mode;
	}

	private IActionSource getActionSource()
	{
		return this.mySource;
	}

	@Override
	public void setActionSource( final IActionSource mySource )
	{
		this.mySource = mySource;
	}

	private static class CachedItemStack
	{

		private final ItemStack itemStack;
		private final IAEItemStack aeStack;

		public CachedItemStack( final ItemStack is )
		{
			if( is.isEmpty() )
			{
				this.itemStack = ItemStack.EMPTY;
				this.aeStack = null;
			}
			else
			{
				this.itemStack = is.copy();
				this.aeStack = AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createStack( is );
			}
		}
	}
}
