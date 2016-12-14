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


import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.me.storage.ITickingMonitor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;


/**
 * Wraps an Item Handler in such a way that it can be used as an IMEInventory for items.
 */
class ItemHandlerAdapter implements IMEInventory<IAEItemStack>, IBaseMonitor<IAEItemStack>, ITickingMonitor
{

	private final Map<IMEMonitorHandlerReceiver<IAEItemStack>, Object> listeners = new HashMap<>();

	private BaseActionSource mySource;

	private final IItemHandler itemHandler;

	private ItemStack[] cachedStacks = new ItemStack[0];

	private IAEItemStack[] cachedAeStacks = new IAEItemStack[0];

	ItemHandlerAdapter( IItemHandler itemHandler )
	{
		this.itemHandler = itemHandler;
	}

	@Override
	public IAEItemStack injectItems( IAEItemStack iox, Actionable type, BaseActionSource src )
	{
		ItemStack orgInput = iox.getItemStack();
		ItemStack remaining = orgInput;

		int slotCount = itemHandler.getSlots();
		boolean simulate = ( type == Actionable.SIMULATE );

		// This uses a brute force approach and tries to jam it in every slot the inventory exposes.
		for( int i = 0; i < slotCount && remaining != null; i++ )
		{
			remaining = itemHandler.insertItem( i, remaining, simulate );
		}

		// At this point, we still have some items left...
		if( remaining == orgInput )
		{
			// The stack remained unmodified, target inventory is full
			return iox;
		}

		if( type == Actionable.MODULATE )
		{
			this.onTick();
		}

		return AEItemStack.create( remaining );
	}

	@Override
	public IAEItemStack extractItems( IAEItemStack request, Actionable mode, BaseActionSource src )
	{

		ItemStack requestedItemStack = request.getItemStack();
		int remainingSize = requestedItemStack.stackSize;

		// Use this to gather the requested items
		ItemStack gathered = null;

		final boolean simulate = ( mode == Actionable.SIMULATE );

		for( int i = 0; i < itemHandler.getSlots(); i++ )
		{
			ItemStack stackInInventorySlot = itemHandler.getStackInSlot( i );

			if( !Platform.itemComparisons().isSameItem( stackInInventorySlot, requestedItemStack ) )
			{
				continue;
			}

			ItemStack extracted;
			int stackSizeCurrentSlot = stackInInventorySlot.stackSize;
			int remainingCurrentSlot = Math.min( remainingSize, stackSizeCurrentSlot );

			// We have to loop here because according to the docs, the handler shouldn't return a stack with size >
			// maxSize, even if we request more. So even if it returns a valid stack, it might have more stuff.
			do
			{
				extracted = itemHandler.extractItem( i, remainingCurrentSlot, simulate );
				if( extracted != null )
				{
					if( extracted.stackSize > remainingCurrentSlot )
					{
						// Something broke. It should never return more than we requested...
						// We're going to silently eat the remainder
						AELog.warn( "Mod that provided item handler %1 is broken. Returned %2 items, even though we requested %3.",
								itemHandler.getClass().getSimpleName(), extracted.stackSize, remainingCurrentSlot );
						extracted.stackSize = remainingCurrentSlot;
					}

					// We're just gonna use the first stack we get our hands on as the template for the rest
					if( gathered == null )
					{
						gathered = extracted;
					}
					else
					{
						gathered.stackSize += extracted.stackSize;
					}
					remainingCurrentSlot -= extracted.stackSize;
				}
			}
			while( extracted != null && remainingCurrentSlot > 0 );

			remainingSize -= stackSizeCurrentSlot - remainingCurrentSlot;

			// Done?
			if( remainingSize <= 0 )
			{
				break;
			}
		}

		if( gathered != null )
		{
			if( mode == Actionable.MODULATE )
			{
				this.onTick();
			}

			return AEItemStack.create( gathered );
		}

		return null;
	}

	@Override
	public TickRateModulation onTick()
	{
		LinkedList<IAEItemStack> changes = new LinkedList<>();

		int slots = itemHandler.getSlots();

		// Make room for new slots
		if( slots > cachedStacks.length )
		{
			cachedStacks = Arrays.copyOf( cachedStacks, slots );
			cachedAeStacks = Arrays.copyOf( cachedAeStacks, slots );
		}

		for( int slot = 0; slot < slots; slot++ )
		{
			// Save the old stuff
			ItemStack oldIS = cachedStacks[slot];
			IAEItemStack oldAeIS = cachedAeStacks[slot];

			ItemStack newIS = itemHandler.getStackInSlot( slot );

			if( this.isDifferent( newIS, oldIS ) )
			{
				addItemChange( slot, oldAeIS, newIS, changes );
			}
			else if( newIS != null && oldIS != null )
			{
				addPossibleStackSizeChange( slot, oldAeIS, newIS, changes );
			}
		}

		// Handle cases where the number of slots actually is lower now than before
		if( slots < cachedStacks.length )
		{
			for( int slot = slots; slot < cachedStacks.length; slot++ )
			{
				IAEItemStack aeStack = cachedAeStacks[slot];
				if( aeStack != null )
				{
					IAEItemStack a = aeStack.copy();
					a.setStackSize( -a.getStackSize() );
					changes.add( a );
				}
			}

			// Reduce the cache size
			cachedStacks = Arrays.copyOf( cachedStacks, slots );
			cachedAeStacks = Arrays.copyOf( cachedAeStacks, slots );
		}

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

	private void addItemChange( int slot, IAEItemStack oldAeIS, ItemStack newIS, List<IAEItemStack> changes )
	{
		// Completely different item
		cachedStacks[slot] = newIS;
		cachedAeStacks[slot] = AEItemStack.create( newIS );

		// If we had a stack previously in this slot, notify the newtork about its disappearance
		if( oldAeIS != null )
		{
			oldAeIS.setStackSize( -oldAeIS.getStackSize() );
			changes.add( oldAeIS );
		}

		// Notify the network about the new stack. Note that this is null if newIS was null
		if( cachedAeStacks[slot] != null )
		{
			changes.add( cachedAeStacks[slot] );
		}
	}

	private void addPossibleStackSizeChange( int slot, IAEItemStack oldAeIS, ItemStack newIS, List<IAEItemStack> changes )
	{
		// Still the same item, but amount might have changed
		long diff = newIS.stackSize - oldAeIS.getStackSize();

		if( diff != 0 )
		{
			IAEItemStack stack = oldAeIS.copy();
			stack.setStackSize( newIS.stackSize );

			cachedStacks[slot] = newIS;
			cachedAeStacks[slot] = stack;

			final IAEItemStack a = stack.copy();
			a.setStackSize( diff );
			changes.add( a );
		}
	}

	private boolean isDifferent( final ItemStack a, final ItemStack b )
	{
		if( a == b && b == null )
		{
			return false;
		}

		return a == null || b == null || !Platform.itemComparisons().isSameItem( a, b );
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
				key.postChange( this, a, mySource );
			}
			else
			{
				i.remove();
			}
		}
	}

	@Override
	public void setActionSource( final BaseActionSource mySource )
	{
		this.mySource = mySource;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems( IItemList<IAEItemStack> out )
	{

		for( int i = 0; i < itemHandler.getSlots(); i++ )
		{
			out.addStorage( AEItemStack.create( itemHandler.getStackInSlot( i ) ) );
		}

		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
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
}
