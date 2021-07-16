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

package appeng.fluids.parts;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.me.GridAccessException;
import appeng.parts.misc.PartStorageBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.fluids.util.AEFluidStack;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.ITickingMonitor;


/**
 * Wraps an Fluid Handler in such a way that it can be used as an IMEInventory for fluids.
 *
 * @author BrockWS
 * @version rv6 - 22/05/2018
 * @since rv6 22/05/2018
 */
public class FluidHandlerAdapter implements IMEInventory<IAEFluidStack>, IBaseMonitor<IAEFluidStack>, ITickingMonitor
{
	private final Map<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> listeners = new HashMap<>();
	private IActionSource source;
	private final IFluidHandler fluidHandler;
	private final IGridProxyable proxyable;
	private final FluidHandlerAdapter.InventoryCache cache;
	private StorageFilter mode;

	FluidHandlerAdapter( IFluidHandler fluidHandler, IGridProxyable proxy )
	{
		this.fluidHandler = fluidHandler;
		this.proxyable = proxy;
		if( this.proxyable instanceof PartStorageBus )
		{
			PartStorageBus partStorageBus = (PartStorageBus) this.proxyable;
			this.mode = ( (StorageFilter) partStorageBus.getConfigManager().getSetting( Settings.STORAGE_FILTER ) );
		}
		this.cache = new FluidHandlerAdapter.InventoryCache( this.fluidHandler, this.mode);
	}

	@Override
	public IAEFluidStack injectItems( IAEFluidStack input, Actionable type, IActionSource src )
	{
		FluidStack fluidStack = input.getFluidStack();

		// Insert
		int wasFillled = this.fluidHandler.fill( fluidStack, type != Actionable.SIMULATE );
		int remaining = fluidStack.amount - wasFillled;
		if( fluidStack.amount == remaining )
		{
			// The stack was unmodified, target tank is full
			return input;
		}

		if( type == Actionable.MODULATE )
		{
			try
			{
				this.proxyable.getProxy().getTick().alertDevice( this.proxyable.getProxy().getNode() );
			}
			catch( GridAccessException ignore )
			{
				// meh
			}
		}

		fluidStack.amount = remaining;

		return AEFluidStack.fromFluidStack( fluidStack );
	}

	@Override
	public IAEFluidStack extractItems( IAEFluidStack request, Actionable mode, IActionSource src )
	{
		FluidStack requestedFluidStack = request.getFluidStack();
		final boolean doDrain = ( mode == Actionable.MODULATE );

		// Drain the fluid from the tank
		FluidStack gathered = this.fluidHandler.drain( requestedFluidStack, doDrain );
		if( gathered == null )
		{
			// If nothing was pulled from the tank, return null
			return null;
		}

		IAEFluidStack gatheredAEFluidstack = AEFluidStack.fromFluidStack( gathered );
		if( mode == Actionable.MODULATE )
		{
			try
			{
				this.proxyable.getProxy().getTick().alertDevice( this.proxyable.getProxy().getNode() );
			}
			catch( GridAccessException ignore )
			{
				// meh
			}
		}
		return gatheredAEFluidstack;
	}

	@Override
	public TickRateModulation onTick()
	{
		List<IAEFluidStack> changes = this.cache.update();
		if( !changes.isEmpty() )
		{
			this.postDifference( changes );
			return TickRateModulation.URGENT;
		}
		else
		{
			return TickRateModulation.SLOWER;
		}
	}

	@Override
	public IItemList<IAEFluidStack> getAvailableItems( IItemList<IAEFluidStack> out )
	{
		return this.cache.getAvailableItems( out );
	}

	@Override
	public IStorageChannel<IAEFluidStack> getChannel()
	{
		return AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class );
	}

	@Override
	public void setActionSource( IActionSource source )
	{
		this.source = source;
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

	private void postDifference( Iterable<IAEFluidStack> a )
	{
		final Iterator<Map.Entry<IMEMonitorHandlerReceiver<IAEFluidStack>, Object>> i = this.listeners.entrySet().iterator();
		while( i.hasNext() )
		{
			final Map.Entry<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> l = i.next();
			final IMEMonitorHandlerReceiver<IAEFluidStack> key = l.getKey();
			if( key.isValid( l.getValue() ) )
			{
				key.postChange( this, a, this.source );
			}
			else
			{
				i.remove();
			}
		}
	}

	private static class InventoryCache
	{
		private final IFluidHandler fluidHandler;
		private final StorageFilter mode;
		IItemList<IAEFluidStack> currentlyCached = AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ).createList();

		public InventoryCache( IFluidHandler fluidHandler, StorageFilter mode )
		{
			this.mode = mode;
			this.fluidHandler = fluidHandler;
		}

		public List<IAEFluidStack> update()
		{
			final List<IAEFluidStack> changes = new ArrayList<>();
			final IFluidTankProperties[] tankProperties = this.fluidHandler.getTankProperties();

			IItemList<IAEFluidStack> currentlyOnStorage = AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ).createList();

			for( IFluidTankProperties tankProperty : tankProperties )
			{
				FluidStack newFS = tankProperty.getContents();
				if( this.mode == StorageFilter.EXTRACTABLE_ONLY && newFS != null )
				{
					if( this.fluidHandler.drain( 1, false ) == null )
					{
						newFS = null;
					}
				}
				if( newFS != null )
				{
					currentlyOnStorage.add( AEFluidStack.fromFluidStack( newFS ) );
				}
			}

			for ( final IAEFluidStack is : currentlyCached )
			{
				is.setStackSize( -is.getStackSize() );
			}

			for ( final IAEFluidStack is : currentlyOnStorage )
			{
				currentlyCached.add( is );
			}

			for ( final IAEFluidStack is : currentlyCached )
			{
				if( is.getStackSize() != 0 )
				{
					changes.add( is );
				}
			}

			currentlyCached = currentlyOnStorage;

			return changes;
		}

		public IItemList<IAEFluidStack> getAvailableItems( IItemList<IAEFluidStack> out )
		{
			currentlyCached.iterator().forEachRemaining( out::add );
			return out;
		}

	}
}
