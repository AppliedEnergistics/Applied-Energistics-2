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
import java.util.List;

import net.minecraft.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.iterators.StackToSlotIterator;


public class AdaptorList extends InventoryAdaptor
{

	private final List<ItemStack> i;

	public AdaptorList( List<ItemStack> s )
	{
		this.i = s;
	}

	@Override
	public ItemStack removeItems( int amount, ItemStack filter, IInventoryDestination destination )
	{
		int s = this.i.size();
		for( int x = 0; x < s; x++ )
		{
			ItemStack is = this.i.get( x );
			if( is != null && ( filter == null || Platform.isSameItemPrecise( is, filter ) ) )
			{
				if( amount > is.stackSize )
					amount = is.stackSize;
				if( destination != null && !destination.canInsert( is ) )
					amount = 0;

				if( amount > 0 )
				{
					ItemStack rv = is.copy();
					rv.stackSize = amount;
					is.stackSize -= amount;

					// remove it..
					if( is.stackSize <= 0 )
						this.i.remove( x );

					return rv;
				}
			}
		}
		return null;
	}

	@Override
	public ItemStack simulateRemove( int amount, ItemStack filter, IInventoryDestination destination )
	{
		for( ItemStack is : this.i )
		{
			if( is != null && ( filter == null || Platform.isSameItemPrecise( is, filter ) ) )
			{
				if( amount > is.stackSize )
				{
					amount = is.stackSize;
				}
				if( destination != null && !destination.canInsert( is ) )
				{
					amount = 0;
				}

				if( amount > 0 )
				{
					ItemStack rv = is.copy();
					rv.stackSize = amount;
					return rv;
				}
			}
		}
		return null;
	}

	@Override
	public ItemStack removeSimilarItems( int how_many, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination )
	{
		int s = this.i.size();
		for( int x = 0; x < s; x++ )
		{
			ItemStack is = this.i.get( x );
			if( is != null && ( filter == null || Platform.isSameItemFuzzy( is, filter, fuzzyMode ) ) )
			{
				if( how_many > is.stackSize )
					how_many = is.stackSize;
				if( destination != null && !destination.canInsert( is ) )
					how_many = 0;

				if( how_many > 0 )
				{
					ItemStack rv = is.copy();
					rv.stackSize = how_many;
					is.stackSize -= how_many;

					// remove it..
					if( is.stackSize <= 0 )
						this.i.remove( x );

					return rv;
				}
			}
		}
		return null;
	}

	@Override
	public ItemStack simulateSimilarRemove( int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination )
	{
		for( ItemStack is : this.i )
		{
			if( is != null && ( filter == null || Platform.isSameItemFuzzy( is, filter, fuzzyMode ) ) )
			{
				if( amount > is.stackSize )
				{
					amount = is.stackSize;
				}
				if( destination != null && !destination.canInsert( is ) )
				{
					amount = 0;
				}

				if( amount > 0 )
				{
					ItemStack rv = is.copy();
					rv.stackSize = amount;
					return rv;
				}
			}
		}
		return null;
	}

	@Override
	public ItemStack addItems( ItemStack toBeAdded )
	{
		if( toBeAdded == null )
			return null;
		if( toBeAdded.stackSize == 0 )
			return null;

		ItemStack left = toBeAdded.copy();

		for( ItemStack is : this.i )
		{
			if( Platform.isSameItem( is, left ) )
			{
				is.stackSize += left.stackSize;
				return null;
			}
		}

		this.i.add( left );
		return null;
	}

	@Override
	public ItemStack simulateAdd( ItemStack toBeSimulated )
	{
		return null;
	}

	@Override
	public boolean containsItems()
	{
		for( ItemStack is : this.i )
		{
			if( is != null )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<ItemSlot> iterator()
	{
		return new StackToSlotIterator( this.i.iterator() );
	}
}
