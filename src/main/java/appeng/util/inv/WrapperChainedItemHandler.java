/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.util.inv;


import java.util.ArrayList;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import appeng.util.helpers.ItemHandlerUtil;


public class WrapperChainedItemHandler implements IInternalItemHandler
{
	private IItemHandler[] itemHandler; // the handlers
	private int[] baseIndex; // index-offsets of the different handlers
	private int slotCount; // number of total slots

	public WrapperChainedItemHandler( IItemHandler... itemHandler )
	{
		setItemHandlers( itemHandler );
	}

	private void setItemHandlers( IItemHandler[] handlers )
	{
		this.itemHandler = handlers;
		this.baseIndex = new int[itemHandler.length];
		int index = 0;
		for( int i = 0; i < itemHandler.length; i++ )
		{
			index += itemHandler[i].getSlots();
			baseIndex[i] = index;
		}
		this.slotCount = index;
	}

	// returns the handler index for the slot
	private int getIndexForSlot( int slot )
	{
		if( slot < 0 )
		{
			return -1;
		}

		for( int i = 0; i < baseIndex.length; i++ )
		{
			if( slot - baseIndex[i] < 0 )
			{
				return i;
			}
		}
		return -1;
	}

	private IItemHandler getHandlerFromIndex( int index )
	{
		if( index < 0 || index >= itemHandler.length )
		{
			return EmptyHandler.INSTANCE;
		}
		return itemHandler[index];
	}

	private int getSlotFromIndex( int slot, int index )
	{
		if( index <= 0 || index >= baseIndex.length )
		{
			return slot;
		}
		return slot - baseIndex[index - 1];
	}

	public void cycleOrder()
	{
		if( this.itemHandler.length > 1 )
		{
			ArrayList<IItemHandler> newOrder = new ArrayList<>();
			newOrder.add( this.itemHandler[this.itemHandler.length - 1] );
			for( int i = 0; i < this.itemHandler.length - 1; ++i )
			{
				newOrder.add( this.itemHandler[i] );
			}
			this.setItemHandlers( newOrder.toArray( new IItemHandler[this.itemHandler.length] ) );
		}
	}

	@Override
	public int getSlots()
	{
		return slotCount;
	}

	@Override
	@Nonnull
	public ItemStack getStackInSlot( final int slot )
	{
		int index = getIndexForSlot( slot );
		IItemHandler handler = getHandlerFromIndex( index );
		int targetSlot = getSlotFromIndex( slot, index );
		return handler.getStackInSlot( targetSlot );
	}

	@Override
	@Nonnull
	public ItemStack insertItem( final int slot, @Nonnull ItemStack stack, boolean simulate )
	{
		int index = getIndexForSlot( slot );
		IItemHandler handler = getHandlerFromIndex( index );
		int targetSlot = getSlotFromIndex( slot, index );
		return handler.insertItem( targetSlot, stack, simulate );
	}

	@Override
	@Nonnull
	public ItemStack extractItem( int slot, int amount, boolean simulate )
	{
		int index = getIndexForSlot( slot );
		IItemHandler handler = getHandlerFromIndex( index );
		int targetSlot = getSlotFromIndex( slot, index );
		return handler.extractItem( targetSlot, amount, simulate );
	}

	@Override
	public int getSlotLimit( int slot )
	{
		int index = getIndexForSlot( slot );
		IItemHandler handler = getHandlerFromIndex( index );
		int localSlot = getSlotFromIndex( slot, index );
		return handler.getSlotLimit( localSlot );
	}

	@Override
	public void setStackInSlot( int slot, ItemStack stack )
	{
		int index = getIndexForSlot( slot );
		IItemHandler handler = getHandlerFromIndex( index );
		int targetSlot = getSlotFromIndex( slot, index );
		ItemHandlerUtil.setStackInSlot( handler, targetSlot, stack );
	}

	@Override
	public boolean isItemValidForSlot( int slot, ItemStack stack )
	{
		int index = getIndexForSlot( slot );
		IItemHandler handler = getHandlerFromIndex( index );
		int targetSlot = getSlotFromIndex( slot, index );
		return ItemHandlerUtil.isItemValidForSlot( handler, targetSlot, stack );
	}

	@Override
	public void markDirty( int slot )
	{
		int index = getIndexForSlot( slot );
		IItemHandler handler = getHandlerFromIndex( index );
		int targetSlot = getSlotFromIndex( slot, index );
		ItemHandlerUtil.markDirty( handler, targetSlot );
	}
}
