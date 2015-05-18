/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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
public final class AdaptorPlayerHand extends InventoryAdaptor
{

	private final EntityPlayer player;

	public AdaptorPlayerHand( EntityPlayer player )
	{
		this.player = player;
	}

	@Override
	public ItemStack removeItems( int amount, ItemStack filter, IInventoryDestination destination )
	{
		ItemStack hand = this.player.inventory.getItemStack();
		if( hand == null )
		{
			return null;
		}

		if( filter == null || Platform.isSameItemPrecise( filter, hand ) )
		{
			ItemStack result = hand.copy();
			result.stackSize = hand.stackSize > amount ? amount : hand.stackSize;
			hand.stackSize -= amount;
			if( hand.stackSize <= 0 )
			{
				this.player.inventory.setItemStack( null );
			}
			return result;
		}

		return null;
	}

	@Override
	public ItemStack simulateRemove( int amount, ItemStack filter, IInventoryDestination destination )
	{

		ItemStack hand = this.player.inventory.getItemStack();
		if( hand == null )
		{
			return null;
		}

		if( filter == null || Platform.isSameItemPrecise( filter, hand ) )
		{
			ItemStack result = hand.copy();
			result.stackSize = hand.stackSize > amount ? amount : hand.stackSize;
			return result;
		}

		return null;
	}

	@Override
	public ItemStack removeSimilarItems( int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination )
	{
		ItemStack hand = this.player.inventory.getItemStack();
		if( hand == null )
		{
			return null;
		}

		if( filter == null || Platform.isSameItemFuzzy( filter, hand, fuzzyMode ) )
		{
			ItemStack result = hand.copy();
			result.stackSize = hand.stackSize > amount ? amount : hand.stackSize;
			hand.stackSize -= amount;
			if( hand.stackSize <= 0 )
			{
				this.player.inventory.setItemStack( null );
			}
			return result;
		}

		return null;
	}

	@Override
	public ItemStack simulateSimilarRemove( int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination )
	{

		ItemStack hand = this.player.inventory.getItemStack();
		if( hand == null )
		{
			return null;
		}

		if( filter == null || Platform.isSameItemFuzzy( filter, hand, fuzzyMode ) )
		{
			ItemStack result = hand.copy();
			result.stackSize = hand.stackSize > amount ? amount : hand.stackSize;
			return result;
		}

		return null;
	}

	@Override
	public ItemStack addItems( ItemStack toBeAdded )
	{

		if( toBeAdded == null )
		{
			return null;
		}
		if( toBeAdded.stackSize == 0 )
		{
			return null;
		}
		if( this.player == null )
		{
			return toBeAdded;
		}
		if( this.player.inventory == null )
		{
			return toBeAdded;
		}

		ItemStack hand = this.player.inventory.getItemStack();

		if( hand != null && !Platform.isSameItemPrecise( toBeAdded, hand ) )
		{
			return toBeAdded;
		}

		int original = 0;
		ItemStack newHand = null;
		if( hand == null )
		{
			newHand = toBeAdded.copy();
		}
		else
		{
			newHand = hand;
			original = hand.stackSize;
			newHand.stackSize += toBeAdded.stackSize;
		}

		if( newHand.stackSize > newHand.getMaxStackSize() )
		{
			newHand.stackSize = newHand.getMaxStackSize();
			ItemStack B = toBeAdded.copy();
			B.stackSize -= newHand.stackSize - original;
			this.player.inventory.setItemStack( newHand );
			return B;
		}

		this.player.inventory.setItemStack( newHand );
		return null;
	}

	@Override
	public ItemStack simulateAdd( ItemStack toBeSimulated )
	{
		ItemStack hand = this.player.inventory.getItemStack();
		if( toBeSimulated == null )
		{
			return null;
		}

		if( hand != null && !Platform.isSameItem( toBeSimulated, hand ) )
		{
			return toBeSimulated;
		}

		int original = 0;
		ItemStack newHand = null;
		if( hand == null )
		{
			newHand = toBeSimulated.copy();
		}
		else
		{
			newHand = hand.copy();
			original = hand.stackSize;
			newHand.stackSize += toBeSimulated.stackSize;
		}

		if( newHand.stackSize > newHand.getMaxStackSize() )
		{
			newHand.stackSize = newHand.getMaxStackSize();
			ItemStack B = toBeSimulated.copy();
			B.stackSize -= newHand.stackSize - original;
			return B;
		}

		return null;
	}

	@Override
	public boolean containsItems()
	{
		return this.player.inventory.getItemStack() != null;
	}

	@Override
	public final Iterator<ItemSlot> iterator()
	{
		return new NullIterator<ItemSlot>();
	}
}
