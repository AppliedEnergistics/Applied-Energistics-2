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
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import net.minecraft.item.ItemStack;
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


public class MEMonitorIInventory implements IMEMonitor<IAEItemStack>
{

	final InventoryAdaptor adaptor;
	final IItemList<IAEItemStack> list = AEApi.instance().storage().createItemList();
	final HashMap<IMEMonitorHandlerReceiver<IAEItemStack>, Object> listeners = new HashMap<IMEMonitorHandlerReceiver<IAEItemStack>, Object>();
	private final NavigableMap<Integer, CachedItemStack> memory;
	public BaseActionSource mySource;
	public StorageFilter mode = StorageFilter.EXTRACTABLE_ONLY;

	public MEMonitorIInventory( InventoryAdaptor adaptor )
	{
		this.adaptor = adaptor;
		this.memory = new ConcurrentSkipListMap<Integer, CachedItemStack>();
	}

	@Override
	public void addListener( IMEMonitorHandlerReceiver<IAEItemStack> l, Object verificationToken )
	{
		this.listeners.put( l, verificationToken );
	}

	@Override
	public void removeListener( IMEMonitorHandlerReceiver<IAEItemStack> l )
	{
		this.listeners.remove( l );
	}

	@Override
	public IAEItemStack injectItems( IAEItemStack input, Actionable type, BaseActionSource src )
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
		IAEItemStack o = input.copy();
		o.setStackSize( out.stackSize );
		return o;
	}

	@Override
	public IAEItemStack extractItems( IAEItemStack request, Actionable type, BaseActionSource src )
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
		IAEItemStack o = request.copy();
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
		boolean changed = false;

		LinkedList<IAEItemStack> changes = new LinkedList<IAEItemStack>();

		int high = 0;
		this.list.resetStatus();
		for( ItemSlot is : this.adaptor )
		{
			CachedItemStack old = this.memory.get( is.slot );
			high = Math.max( high, is.slot );

			ItemStack newIS = !is.isExtractable && this.mode == StorageFilter.EXTRACTABLE_ONLY ? null : is.getItemStack();
			ItemStack oldIS = old == null ? null : old.itemStack;

			if( this.isDifferent( newIS, oldIS ) )
			{
				CachedItemStack cis = new CachedItemStack( is.getItemStack() );
				this.memory.put( is.slot, cis );

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
				int newSize = ( newIS == null ? 0 : newIS.stackSize );
				int diff = newSize - ( oldIS == null ? 0 : oldIS.stackSize );

				IAEItemStack stack = ( old == null || old.aeStack == null ? AEApi.instance().storage().createItemStack( newIS ) : old.aeStack.copy() );
				if( stack != null )
				{
					stack.setStackSize( newSize );
					this.list.add( stack );
				}

				if( diff != 0 && stack != null )
				{
					CachedItemStack cis = new CachedItemStack( is.getItemStack() );
					this.memory.put( is.slot, cis );

					IAEItemStack a = stack.copy();
					a.setStackSize( diff );
					changes.add( a );
					changed = true;
				}
			}
		}

		// detect dropped items; should fix non IISided Inventory Changes.
		NavigableMap<Integer, CachedItemStack> end = this.memory.tailMap( high, false );
		if( !end.isEmpty() )
		{
			for( CachedItemStack cis : end.values() )
			{
				if( cis != null && cis.aeStack != null )
				{
					IAEItemStack a = cis.aeStack.copy();
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

	private boolean isDifferent( ItemStack a, ItemStack b )
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

	private void postDifference( Iterable<IAEItemStack> a )
	{
		// AELog.info( a.getItemStack().getUnlocalizedName() + " @ " + a.getStackSize() );
		if( a != null )
		{
			Iterator<Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object>> i = this.listeners.entrySet().iterator();
			while( i.hasNext() )
			{
				Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object> l = i.next();
				IMEMonitorHandlerReceiver<IAEItemStack> key = l.getKey();
				if( key.isValid( l.getValue() ) )
				{
					key.postChange( this, a, this.mySource );
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
	public boolean isPrioritized( IAEItemStack input )
	{
		return false;
	}

	@Override
	public boolean canAccept( IAEItemStack input )
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
	public boolean validForPass( int i )
	{
		return true;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems( IItemList out )
	{
		for( CachedItemStack is : this.memory.values() )
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

	static class CachedItemStack
	{

		final ItemStack itemStack;
		final IAEItemStack aeStack;

		public CachedItemStack( ItemStack is )
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
