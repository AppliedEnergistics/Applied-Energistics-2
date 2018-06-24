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

package appeng.me.storage;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.StorageFilter;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;


public class MEMonitorIFluidHandler implements IMEMonitor<IAEFluidStack>, ITickingMonitor
{
	private final IFluidHandler handler;
	private final IItemList<IAEFluidStack> list = AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ).createList();
	private final HashMap<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> listeners = new HashMap<>();
	private final NavigableMap<Integer, CachedFluidStack> memory;
	private IActionSource mySource;
	private StorageFilter mode = StorageFilter.EXTRACTABLE_ONLY;

	public MEMonitorIFluidHandler( final IFluidHandler handler )
	{
		this.handler = handler;
		this.memory = new ConcurrentSkipListMap<>();
	}

	@Override
	public void addListener( final IMEMonitorHandlerReceiver<IAEFluidStack> l, final Object verificationToken )
	{
		this.listeners.put( l, verificationToken );
	}

	@Override
	public void removeListener( final IMEMonitorHandlerReceiver<IAEFluidStack> l )
	{
		this.listeners.remove( l );
	}

	@Override
	public IAEFluidStack injectItems( final IAEFluidStack input, final Actionable type, final IActionSource src )
	{
		final int filled = this.handler.fill( input.getFluidStack(), type == Actionable.MODULATE );

		if( filled == 0 )
		{
			return input.copy();
		}

		if( type == Actionable.MODULATE )
		{
			this.onTick();
		}

		if( filled == input.getStackSize() )
		{
			return null;
		}

		final IAEFluidStack o = input.copy();
		o.setStackSize( input.getStackSize() - filled );
		return o;
	}

	@Override
	public IAEFluidStack extractItems( final IAEFluidStack request, final Actionable type, final IActionSource src )
	{
		final FluidStack removed = this.handler.drain( request.getFluidStack(), type == Actionable.MODULATE );

		if( removed == null || removed.amount == 0 )
		{
			return null;
		}

		if( type == Actionable.MODULATE )
		{
			this.onTick();
		}

		final IAEFluidStack o = request.copy();
		o.setStackSize( removed.amount );
		return o;
	}

	@Override
	public IStorageChannel getChannel()
	{
		return AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class );
	}

	@Override
	public TickRateModulation onTick()
	{
		final List<IAEFluidStack> changes = new ArrayList<>();

		this.list.resetStatus();
		int high = 0;
		boolean changed = false;

		final IFluidTankProperties[] props = this.handler.getTankProperties();
		for( int slot = 0; slot < this.handler.getTankProperties().length; ++slot )
		{
			final CachedFluidStack old = this.memory.get( slot );
			high = Math.max( high, slot );

			final FluidStack newIS = !props[slot].canDrain() && this.getMode() == StorageFilter.EXTRACTABLE_ONLY ? null : props[slot].getContents();
			final FluidStack oldIS = old == null ? null : old.fluidStack;

			if( isDifferent( newIS, oldIS ) )
			{
				final CachedFluidStack cis = new CachedFluidStack( newIS );
				this.memory.put( slot, cis );

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
				final int newSize = newIS == null ? 0 : newIS.amount;
				final int diff = newSize - ( oldIS == null ? 0 : oldIS.amount );

				IAEFluidStack stack = null;

				if( newIS != null )
				{
					stack = ( old == null || old.aeStack == null ? AEApi.instance()
							.storage()
							.getStorageChannel( IFluidStorageChannel.class )
							.createStack( newIS ) : old.aeStack.copy() );
				}
				if( stack != null )
				{
					stack.setStackSize( newSize );
					this.list.add( stack );
				}

				if( diff != 0 && stack != null )
				{
					final CachedFluidStack cis = new CachedFluidStack( newIS );
					this.memory.put( slot, cis );

					final IAEFluidStack a = stack.copy();
					a.setStackSize( diff );
					changes.add( a );
					changed = true;
				}
			}
		}

		// detect dropped items; should fix non IISided Inventory Changes.
		final NavigableMap<Integer, CachedFluidStack> end = this.memory.tailMap( high, false );
		if( !end.isEmpty() )
		{
			for( final CachedFluidStack cis : end.values() )
			{
				if( cis != null && cis.aeStack != null )
				{
					final IAEFluidStack a = cis.aeStack.copy();
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

	private static boolean isDifferent( FluidStack a, FluidStack b )
	{
		if( a == b )
		{
			return false;
		}
		if( a == null || b == null )
		{
			return true;
		}
		return !a.getFluid().equals( b.getFluid() );
	}

	private void postDifference( final Iterable<IAEFluidStack> a )
	{
		if( a != null )
		{
			final Iterator<Entry<IMEMonitorHandlerReceiver<IAEFluidStack>, Object>> i = this.listeners.entrySet().iterator();
			while( i.hasNext() )
			{
				final Entry<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> l = i.next();
				final IMEMonitorHandlerReceiver<IAEFluidStack> key = l.getKey();
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
		return true;
	}

	@Override
	public IItemList<IAEFluidStack> getAvailableItems( final IItemList out )
	{
		for( final CachedFluidStack is : this.memory.values() )
		{
			out.addStorage( is.aeStack );
		}

		return out;
	}

	@Override
	public IItemList<IAEFluidStack> getStorageList()
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

	private IActionSource getActionSource()
	{
		return this.mySource;
	}

	@Override
	public void setActionSource( final IActionSource mySource )
	{
		this.mySource = mySource;
	}

	private static class CachedFluidStack
	{

		private final FluidStack fluidStack;
		private final IAEFluidStack aeStack;

		CachedFluidStack( final FluidStack is )
		{
			if( is == null )
			{
				this.fluidStack = null;
				this.aeStack = null;
			}
			else
			{
				this.fluidStack = is.copy();
				this.aeStack = AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ).createStack( is );
			}
		}
	}
}
