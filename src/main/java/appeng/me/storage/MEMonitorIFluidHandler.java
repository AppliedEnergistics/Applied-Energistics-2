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


import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

import appeng.fluids.util.AEFluidStack;
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
	private IItemList<IAEFluidStack> cache = AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ).createList();
	private final HashMap<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> listeners = new HashMap<>();
	private IActionSource mySource;
	private StorageFilter mode = StorageFilter.EXTRACTABLE_ONLY;

	public MEMonitorIFluidHandler( final IFluidHandler handler )
	{
		this.handler = handler;
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

		if( filled == input.getStackSize() )
		{
			return null;
		}

		final IAEFluidStack o = input.copy();
		o.setStackSize( input.getStackSize() - filled );

		if( type == Actionable.MODULATE )
		{
			IAEFluidStack added = o.copy();
			this.cache.add( added );
			this.postDifference( Collections.singletonList( added ) );
			this.onTick();
		}

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

		final IAEFluidStack o = request.copy();
		o.setStackSize( removed.amount );

		if( type == Actionable.MODULATE )
		{
			IAEFluidStack cachedStack = this.cache.findPrecise( request );
			if( cachedStack != null )
			{
				cachedStack.decStackSize( o.getStackSize() );
				this.postDifference( Collections.singletonList( o.copy().setStackSize( -o.getStackSize() ) ) );
			}
		}
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
		boolean changed = false;

		final List<IAEFluidStack> changes = new ArrayList<>();
		final IFluidTankProperties[] tankProperties = this.handler.getTankProperties();

		IItemList<IAEFluidStack> currentlyOnStorage = AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ).createList();

		for( IFluidTankProperties tankProperty : tankProperties )
		{
			if( this.mode == StorageFilter.EXTRACTABLE_ONLY && this.handler.drain( 1, false ) == null )
			{
				continue;
			}
			currentlyOnStorage.add( AEFluidStack.fromFluidStack( tankProperty.getContents() ) );
		}

		for( final IAEFluidStack is : cache )
		{
			is.setStackSize( -is.getStackSize() );
		}

		for( final IAEFluidStack is : currentlyOnStorage )
		{
			cache.add( is );
		}

		for( final IAEFluidStack is : cache )
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
		for( final IAEFluidStack fs : cache )
		{
			out.addStorage( fs );
		}

		return out;
	}

	@Override
	public IItemList<IAEFluidStack> getStorageList()
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

}
