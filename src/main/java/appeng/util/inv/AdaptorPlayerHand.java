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

package appeng.util.inv;


import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.iterators.NullIterator;


/*
 * Lets you do simply tests with the players cursor, without messing with the specifics.
 */
public class AdaptorPlayerHand extends InventoryAdaptor
{

	private final EntityPlayer player;

	public AdaptorPlayerHand( final EntityPlayer player )
	{
		this.player = player;
	}

	@Override
	public ItemStack removeItems( final int amount, final ItemStack filter, final IInventoryDestination destination )
	{
		final ItemStack hand = this.player.inventory.getItemStack();
		if( hand.isEmpty() )
		{
			return ItemStack.EMPTY;
		}

		if( filter.isEmpty() || Platform.itemComparisons().isSameItem( filter, hand ) )
		{
			final ItemStack result = hand.copy();
			result.setCount( hand.getCount() > amount ? amount : hand.getCount() );
			hand.grow( -amount );
			if( hand.getCount() <= 0 )
			{
				this.player.inventory.setItemStack( ItemStack.EMPTY );
			}
			return result;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack simulateRemove( final int amount, final ItemStack filter, final IInventoryDestination destination )
	{

		final ItemStack hand = this.player.inventory.getItemStack();
		if( hand.isEmpty() )
		{
			return ItemStack.EMPTY;
		}

		if( filter == null || Platform.itemComparisons().isSameItem( filter, hand ) )
		{
			final ItemStack result = hand.copy();
			result.setCount( hand.getCount() > amount ? amount : hand.getCount() );
			return result;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeSimilarItems( final int amount, final ItemStack filter, final FuzzyMode fuzzyMode, final IInventoryDestination destination )
	{
		final ItemStack hand = this.player.inventory.getItemStack();
		if( hand.isEmpty() )
		{
			return ItemStack.EMPTY;
		}

		if( filter.isEmpty() || Platform.itemComparisons().isFuzzyEqualItem( filter, hand, fuzzyMode ) )
		{
			final ItemStack result = hand.copy();
			result.setCount( hand.getCount() > amount ? amount : hand.getCount() );
			hand.grow( -amount );
			if( hand.getCount() <= 0 )
			{
				this.player.inventory.setItemStack( ItemStack.EMPTY );
			}
			return result;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack simulateSimilarRemove( final int amount, final ItemStack filter, final FuzzyMode fuzzyMode, final IInventoryDestination destination )
	{

		final ItemStack hand = this.player.inventory.getItemStack();
		if( hand.isEmpty() )
		{
			return ItemStack.EMPTY;
		}

		if( filter.isEmpty() || Platform.itemComparisons().isFuzzyEqualItem( filter, hand, fuzzyMode ) )
		{
			final ItemStack result = hand.copy();
			result.setCount( hand.getCount() > amount ? amount : hand.getCount() );
			return result;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack addItems( final ItemStack toBeAdded )
	{

		if( toBeAdded.isEmpty() )
		{
			return ItemStack.EMPTY;
		}
		if( toBeAdded.getCount() == 0 )
		{
			return ItemStack.EMPTY;
		}
		if( this.player == null )
		{
			return toBeAdded;
		}
		if( this.player.inventory == null )
		{
			return toBeAdded;
		}

		final ItemStack hand = this.player.inventory.getItemStack();

		if( !hand.isEmpty() && !Platform.itemComparisons().isSameItem( toBeAdded, hand ) )
		{
			return toBeAdded;
		}

		int original = 0;
		ItemStack newHand = ItemStack.EMPTY;
		if( hand.isEmpty() )
		{
			newHand = toBeAdded.copy();
		}
		else
		{
			newHand = hand;
			original = hand.getCount();
			newHand.grow( toBeAdded.getCount() );
		}

		if( newHand.getCount() > newHand.getMaxStackSize() )
		{
			newHand.setCount( newHand.getMaxStackSize() );
			final ItemStack B = toBeAdded.copy();
			B.grow( -( newHand.getCount() - original ) );
			this.player.inventory.setItemStack( newHand );
			return B;
		}

		this.player.inventory.setItemStack( newHand );
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack simulateAdd( final ItemStack toBeSimulated )
	{
		final ItemStack hand = this.player.inventory.getItemStack();
		if( toBeSimulated.isEmpty() )
		{
			return ItemStack.EMPTY;
		}

		if( !hand.isEmpty() && !Platform.itemComparisons().isEqualItem( toBeSimulated, hand ) )
		{
			return toBeSimulated;
		}

		int original = 0;
		ItemStack newHand = ItemStack.EMPTY;
		if( hand.isEmpty() )
		{
			newHand = toBeSimulated.copy();
		}
		else
		{
			newHand = hand.copy();
			original = hand.getCount();
			newHand.grow( toBeSimulated.getCount() );
		}

		if( newHand.getCount() > newHand.getMaxStackSize() )
		{
			newHand.setCount( newHand.getMaxStackSize() );
			final ItemStack B = toBeSimulated.copy();
			B.grow( -( newHand.getCount() - original ) );
			return B;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public boolean containsItems()
	{
		return !this.player.inventory.getItemStack().isEmpty();
	}

	@Override
	public Iterator<ItemSlot> iterator()
	{
		return new NullIterator<ItemSlot>();
	}
}
