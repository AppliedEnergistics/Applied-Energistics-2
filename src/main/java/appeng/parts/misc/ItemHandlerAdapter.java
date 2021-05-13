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

package appeng.parts.misc;


import java.util.*;

import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.ITickingMonitor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import org.apache.commons.lang3.tuple.Pair;


/**
 * Wraps an Item Handler in such a way that it can be used as an IMEInventory for items.
 */
class ItemHandlerAdapter implements IMEInventory<IAEItemStack>, IBaseMonitor<IAEItemStack>, ITickingMonitor
{
	private final Object2ObjectMap<IMEMonitorHandlerReceiver<IAEItemStack>, Object> listeners = new Object2ObjectOpenHashMap<>();
	private IActionSource mySource;
	private final IItemHandler itemHandler;
	private final IGridProxyable proxyable;
	private final InventoryCache cache;
	private StorageFilter mode;

	ItemHandlerAdapter( IItemHandler itemHandler, IGridProxyable proxy )
	{
		this.itemHandler = itemHandler;
		this.proxyable = proxy;
		if( this.proxyable instanceof PartStorageBus )
		{
			PartStorageBus partStorageBus = (PartStorageBus) this.proxyable;
			this.mode = ( (StorageFilter) partStorageBus.getConfigManager().getSetting( Settings.STORAGE_FILTER ) );
		}
		this.cache = new InventoryCache( this.itemHandler, this.mode );
	}

	@Override
	public IAEItemStack injectItems( IAEItemStack iox, Actionable type, IActionSource src )
	{

		ItemStack orgInput = iox.createItemStack();
		ItemStack remaining = orgInput;

		int slotCount = this.itemHandler.getSlots();
		boolean simulate = ( type == Actionable.SIMULATE );
		List<Pair<Integer, Integer>> injectedList = new ArrayList<>();
		// This uses a brute force approach and tries to jam it in every slot the inventory exposes.
		for( int i = 0; i < slotCount && !remaining.isEmpty(); i++ )
		{
			int countPre = remaining.getCount();
			remaining = this.itemHandler.insertItem( i, remaining, simulate );
			int countPos = remaining.getCount();
			if( remaining.isEmpty() )
			{
				injectedList.add( Pair.of( i, countPre ) );
				break;
			}
			else if( countPos < countPre )
			{
				injectedList.add( Pair.of( i, countPre - countPos ) );
			}
		}

		// At this point, we still have some items left...
		if( remaining == orgInput )
		{
			// The stack remained unmodified, target inventory is full
			return iox;
		}

		if( type == Actionable.MODULATE )
		{
			if( this.cache.cachedAeStacks.length == 0 )
			{
				this.cache.update();
			}
			else
			{
				for( Pair<Integer, Integer> pair : injectedList )
				{
					if( pair.getKey() >= this.cache.cachedAeStacks.length )
					{
						this.cache.update();
						break;
					}
					if( this.cache.cachedAeStacks[pair.getKey()] == null )
					{
						this.cache.cachedAeStacks[pair.getKey()] = iox.copy().setStackSize( pair.getValue() );
					}
					else
					{
						this.cache.cachedAeStacks[pair.getKey()].incStackSize( pair.getValue() );
					}
				}
			}

		}

		return AEItemStack.fromItemStack( remaining );
	}

	@Override
	public IAEItemStack extractItems( IAEItemStack request, Actionable mode, IActionSource src )
	{
		ItemStack requestedItemStack = request.createItemStack();
		int remainingSize = requestedItemStack.getCount();

		// Use this to gather the requested items
		ItemStack gathered = ItemStack.EMPTY;

		final boolean simulate = ( mode == Actionable.SIMULATE );
		List<Pair<Integer, Integer>> extractedList = new ArrayList<>();


		for ( int i = 0; i < this.itemHandler.getSlots(); i++ )
		{
			ItemStack stackInInventorySlot = this.itemHandler.getStackInSlot( i );

			if( !Platform.itemComparisons().isSameItem( stackInInventorySlot, requestedItemStack ) )
			{
				continue;
			}

			ItemStack extracted;
			int stackSizeCurrentSlot = stackInInventorySlot.getCount();
			int remainingCurrentSlot = Math.min( remainingSize, stackSizeCurrentSlot );

			// We have to loop here because according to the docs, the handler shouldn't return a stack with size >
			// maxSize, even if we request more. So even if it returns a valid stack, it might have more stuff.
			do
			{
				extracted = this.itemHandler.extractItem( i, remainingCurrentSlot, simulate );

				if( !extracted.isEmpty() )
				{
					if( extracted.getCount() > remainingCurrentSlot )
					{
						// Something broke. It should never return more than we requested...
						// We're going to silently eat the remainder
						AELog.warn( "Mod that provided item handler %s is broken. Returned %s items while only requesting %d.",
								this.itemHandler.getClass().getName(), extracted.toString(), remainingCurrentSlot );
						extracted.setCount( remainingCurrentSlot );
					}

					// We're just gonna use the first stack we get our hands on as the template for the rest.
					// In case some stupid itemhandler (aka forge) returns an internal state we have to do a second
					// expensive copy again.
					if( gathered.isEmpty() )
					{
						gathered = extracted.copy();
					}
					else
					{
						gathered.grow( extracted.getCount() );
					}
					remainingCurrentSlot -= extracted.getCount();
				}
			}
			while ( !extracted.isEmpty() && remainingCurrentSlot > 0 );

			remainingSize -= stackSizeCurrentSlot - remainingCurrentSlot;

			if (!gathered.isEmpty())
			{
				extractedList.add( Pair.of( i, gathered.getCount() ) );
			}

			// Done?
			if( remainingSize <= 0 )
			{
				break;
			}
		}

		if( !gathered.isEmpty() )
		{
			if( mode == Actionable.MODULATE )
			{
				if( this.cache.cachedAeStacks.length == 0 )
				{
					this.cache.update();
				}
				else
				{
					for( Pair<Integer, Integer> pair : extractedList )
					{
						if( this.cache.cachedAeStacks[pair.getKey()] != null )
						{
							this.cache.cachedAeStacks[pair.getKey()].decStackSize( pair.getValue() );
							if( this.cache.cachedAeStacks[pair.getKey()].getStackSize() == 0 )
							{
								this.cache.cachedAeStacks[pair.getKey()] = null;
							}
						}
					}
				}
			}

			return AEItemStack.fromItemStack( gathered );
		}

		return null;
	}

	@Override
	public TickRateModulation onTick()
	{
		List<IAEItemStack> changes = this.cache.update();
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
	public void setActionSource( final IActionSource mySource )
	{
		this.mySource = mySource;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems( IItemList<IAEItemStack> out )
	{
		return this.cache.getAvailableItems( out );
	}

	@Override
	public IItemStorageChannel getChannel()
	{
		return AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class );
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

	private void postDifference( Iterable<IAEItemStack> a )
	{
		final Iterator<Map.Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object>> i = this.listeners.entrySet().iterator();
		while( i.hasNext() )
		{
			final Map.Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object> l = i.next();
			final IMEMonitorHandlerReceiver<IAEItemStack> key = l.getKey();
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

	private static class InventoryCache
	{
		private IAEItemStack[] cachedAeStacks = new IAEItemStack[0];
		private final IItemHandler itemHandler;
		private final StorageFilter mode;

		public InventoryCache( IItemHandler itemHandler, StorageFilter mode )
		{
			this.mode = mode;
			this.itemHandler = itemHandler;
		}

		public IItemList<IAEItemStack> getAvailableItems( IItemList<IAEItemStack> out )
		{
			Arrays.stream( this.cachedAeStacks ).forEach( out::add );
			return out;
		}

		public List<IAEItemStack> update()
		{
			final List<IAEItemStack> changes = new ArrayList<>();
			final int slots = this.itemHandler.getSlots();

			// Make room for new slots
			if( slots > this.cachedAeStacks.length )
			{
				this.cachedAeStacks = Arrays.copyOf( this.cachedAeStacks, slots );
			}

			for( int slot = 0; slot < slots; slot++ )
			{
				// Save the old stuff
				final IAEItemStack oldAeIS = this.cachedAeStacks[slot];
				ItemStack newIS = this.itemHandler.getStackInSlot( slot );
				if( this.mode == StorageFilter.EXTRACTABLE_ONLY && !newIS.isEmpty() )
				{
					if( this.itemHandler.extractItem( slot, 1, true ).isEmpty() )
					{
						newIS = ItemStack.EMPTY;
					}
				}
				this.handlePossibleSlotChanges( slot, oldAeIS, newIS, changes );
			}

			// Handle cases where the number of slots actually is lower now than before
			if( slots < this.cachedAeStacks.length )
			{
				for( int slot = slots; slot < this.cachedAeStacks.length; slot++ )
				{
					final IAEItemStack aeStack = this.cachedAeStacks[slot];

					if( aeStack != null )
					{
						final IAEItemStack a = aeStack.copy();
						a.setStackSize( -a.getStackSize() );
						changes.add( a );
					}
				}

				// Reduce the cache size
				this.cachedAeStacks = Arrays.copyOf( this.cachedAeStacks, slots );
			}

			return changes;
		}

		private void handlePossibleSlotChanges( int slot, IAEItemStack oldAeIS, ItemStack newIS, List<IAEItemStack> changes )
		{
			if( oldAeIS != null && oldAeIS.isSameType( newIS ) )
			{
				this.handleStackSizeChanged( slot, oldAeIS, newIS, changes );
			}
			else
			{
				this.handleItemChanged( slot, oldAeIS, newIS, changes );
			}
		}

		private void handleStackSizeChanged( int slot, IAEItemStack oldAeIS, ItemStack newIS, List<IAEItemStack> changes )
		{
			// Still the same item, but amount might have changed
			final long diff = newIS.getCount() - oldAeIS.getStackSize();

			if( diff != 0 )
			{
				final IAEItemStack stack = oldAeIS.copy();
				stack.setStackSize( newIS.getCount() );

				this.cachedAeStacks[slot] = stack;

				final IAEItemStack a = stack.copy();
				a.setStackSize( diff );
				changes.add( a );
			}
		}

		private void handleItemChanged( int slot, IAEItemStack oldAeIS, ItemStack newIS, List<IAEItemStack> changes )
		{
			// Completely different item
			this.cachedAeStacks[slot] = AEItemStack.fromItemStack( newIS );

			// If we had a stack previously in this slot, notify the network about its disappearance
			if( oldAeIS != null )
			{
				oldAeIS.setStackSize( -oldAeIS.getStackSize() );
				changes.add( oldAeIS );
			}

			// Notify the network about the new stack. Note that this is null if newIS was null
			if( this.cachedAeStacks[slot] != null )
			{
				changes.add( this.cachedAeStacks[slot] );
			}
		}
	}

}
