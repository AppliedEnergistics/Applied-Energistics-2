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


import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.StorageFilter;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.ItemSlot;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;


public class MEMonitorIInventory implements IMEMonitor<IAEItemStack>
{

	private final InventoryAdaptor adaptor;
	private final IItemList<IAEItemStack> list = AEApi.instance().storage().createItemList();
	private final HashMap<IMEMonitorHandlerReceiver<IAEItemStack>, Object> listeners = new HashMap<IMEMonitorHandlerReceiver<IAEItemStack>, Object>();
	private final NavigableMap<Integer, CachedItemStack> memory;
	private BaseActionSource mySource;
	private StorageFilter mode = StorageFilter.EXTRACTABLE_ONLY;

	public MEMonitorIInventory( final InventoryAdaptor adaptor )
	{
		this.adaptor = adaptor;
		this.memory = new ConcurrentSkipListMap<Integer, CachedItemStack>();
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
	public IAEItemStack injectItems( final IAEItemStack input, final Actionable type, final BaseActionSource src )
	{
		ItemStack out = null;

		if( type == Actionable.SIMULATE )
		{
			out = this.adaptor.simulateAdd( input.getItemStack() );
		}
		else
		{
			out = this.adaptor.addItems( input.getItemStack() );
		}

		if( type == Actionable.MODULATE )
		{
			this.onTick();
		}

		if( out == null )
		{
			return null;
		}

		// better then doing construction from scratch :3
		final IAEItemStack o = input.copy();
		o.setStackSize( out.stackSize );
		return o;
	}

	@Override
	public IAEItemStack extractItems( final IAEItemStack request, final Actionable type, final BaseActionSource src )
	{
		ItemStack out = null;

		if( type == Actionable.SIMULATE )
		{
			out = this.adaptor.simulateRemove( (int) request.getStackSize(), request.getItemStack(), null );
		}
		else
		{
			out = this.adaptor.removeItems( (int) request.getStackSize(), request.getItemStack(), null );
		}

		if( out == null )
		{
			return null;
		}

		// better then doing construction from scratch :3
		final IAEItemStack o = request.copy();
		o.setStackSize( out.stackSize );

		if( type == Actionable.MODULATE )
		{
			this.onTick();
		}

		return o;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	public TickRateModulation onTick()
	{

		final LinkedList<IAEItemStack> changes = new LinkedList<IAEItemStack>();

		this.list.resetStatus();
		int high = 0;
		boolean changed = false;
		for( final ItemSlot is : this.adaptor )
		{
			final CachedItemStack old = this.memory.get( is.getSlot() );
			high = Math.max( high, is.getSlot() );

			final ItemStack newIS = !is.isExtractable() && this.getMode() == StorageFilter.EXTRACTABLE_ONLY ? null : is.getItemStack();
			final ItemStack oldIS = old == null ? null : old.itemStack;

			if( this.isDifferent( newIS, oldIS ) )
			{
				final CachedItemStack cis = new CachedItemStack( is.getItemStack() );
				this.memory.put( is.getSlot(), cis );

				if( old != null && old.aeStack != null )
				{
					old.aeStack.setStackSize( -old.aeStack.getStackSize() );
					changes.add( old.aeStack );
				}

				if( cis.aeStack != null )
				{
					changes.add( cis.aeStack );
					this.list.add( cis.aeStack );
				}

				changed = true;
			}
			else
			{
				final int newSize = ( newIS == null ? 0 : newIS.stackSize );
				final int diff = newSize - ( oldIS == null ? 0 : oldIS.stackSize );

				final IAEItemStack stack = ( old == null || old.aeStack == null ? AEApi.instance().storage().createItemStack( newIS ) : old.aeStack.copy() );
				if( stack != null )
				{
					stack.setStackSize( newSize );
					this.list.add( stack );
				}

				if( diff != 0 && stack != null )
				{
					final CachedItemStack cis = new CachedItemStack( is.getItemStack() );
					this.memory.put( is.getSlot(), cis );

					final IAEItemStack a = stack.copy();
					a.setStackSize( diff );
					changes.add( a );
					changed = true;
				}
			}
		}

		// detect dropped items; should fix non IISided Inventory Changes.
		final NavigableMap<Integer, CachedItemStack> end = this.memory.tailMap( high, false );
		if( !end.isEmpty() )
		{
			for( final CachedItemStack cis : end.values() )
			{
				if( cis != null && cis.aeStack != null )
				{
					final IAEItemStack a = cis.aeStack.copy();
					a.setStackSize( -a.getStackSize() );
					changes.add( a );
					changed = true;
				}
			}
			end.clear();
		}

		if( !changes.isEmpty() )
		{
			this.postDifference( changes );
		}

		return changed ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
	}

	private boolean isDifferent( final ItemStack a, final ItemStack b )
	{
		if( a == b && b == null )
		{
			return false;
		}

		if( ( a == null && b != null ) || ( a != null && b == null ) )
		{
			return true;
		}

		return !Platform.isSameItemPrecise( a, b );
	}

	private void postDifference( final Iterable<IAEItemStack> a )
	{
		// AELog.info( a.getItemStack().getUnlocalizedName() + " @ " + a.getStackSize() );
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
		for( final CachedItemStack is : this.memory.values() )
		{
			out.addStorage( is.aeStack );
		}

		return out;
	}

	@Override
	public IItemList<IAEItemStack> getStorageList()
	{
		return this.list;
	}

	private StorageFilter getMode()
	{
		return this.mode;
	}

	public void setMode( final StorageFilter mode )
	{
		this.mode = mode;
	}

	private BaseActionSource getActionSource()
	{
		return this.mySource;
	}

	public void setActionSource( final BaseActionSource mySource )
	{
		this.mySource = mySource;
	}

	private static class CachedItemStack
	{

		private final ItemStack itemStack;
		private final IAEItemStack aeStack;

		public CachedItemStack( final ItemStack is )
		{
			if( is == null )
			{
				this.itemStack = null;
				this.aeStack = null;
			}
			else
			{
				this.itemStack = is.copy();
				this.aeStack = AEApi.instance().storage().createItemStack( is );
			}
		}
	}
}
