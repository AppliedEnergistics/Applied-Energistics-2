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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;


public class AdaptorItemHandler extends InventoryAdaptor
{
	final static ItemStack integratedCircuit = new ItemStack((Item.getByNameOrId("gregtech:meta_item_1")),1,32766); // Gregtech Community Edition Integrated Circuit
	protected final IItemHandler itemHandler;

	public AdaptorItemHandler( IItemHandler itemHandler )
	{
		this.itemHandler = itemHandler;
	}

	@Override
	public boolean hasSlots()
	{
		return this.itemHandler.getSlots() > 0;
	}

	@Override
	public ItemStack removeItems( int amount, ItemStack filter, IInventoryDestination destination )
	{
		int slots = this.itemHandler.getSlots();
		ItemStack rv = ItemStack.EMPTY;

		for( int slot = 0; slot < slots && amount > 0; slot++ )
		{
			final ItemStack is = this.itemHandler.getStackInSlot( slot );
			if( is.isEmpty() || ( !filter.isEmpty() && !Platform.itemComparisons().isSameItem( is, filter ) ) )
			{
				continue;
			}

			if( destination != null )
			{
				ItemStack extracted = this.itemHandler.extractItem( slot, amount, true );
				if( extracted.isEmpty() )
				{
					continue;
				}

				if( !destination.canInsert( extracted ) )
				{
					continue;
				}
			}

			// Attempt extracting it
			ItemStack extracted = this.itemHandler.extractItem( slot, amount, false );

			if( extracted.isEmpty() )
			{
				continue;
			}

			if( rv.isEmpty() )
			{
				// Use the first stack as a template for the result
				rv = extracted;
				filter = extracted;
				amount -= extracted.getCount();
			}
			else
			{
				// Subsequent stacks will just increase the extracted size
				rv.grow( extracted.getCount() );
				amount -= extracted.getCount();
			}
		}

		return rv;
	}

	@Override
	public ItemStack simulateRemove( int amount, ItemStack filter, IInventoryDestination destination )
	{
		int slots = this.itemHandler.getSlots();
		ItemStack rv = ItemStack.EMPTY;

		for( int slot = 0; slot < slots && amount > 0; slot++ )
		{
			final ItemStack is = this.itemHandler.getStackInSlot( slot );
			if( !is.isEmpty() && ( filter.isEmpty() || Platform.itemComparisons().isSameItem( is, filter ) ) )
			{
				ItemStack extracted = this.itemHandler.extractItem( slot, amount, true );

				if( extracted.isEmpty() )
				{
					continue;
				}

				if( destination != null )
				{
					if( !destination.canInsert( extracted ) )
					{
						continue;
					}
				}

				if( rv.isEmpty() )
				{
					// Use the first stack as a template for the result
					rv = extracted.copy();
					filter = extracted;
					amount -= extracted.getCount();
				}
				else
				{
					// Subsequent stacks will just increase the extracted size
					rv.grow( extracted.getCount() );
					amount -= extracted.getCount();
				}
			}
		}

		return rv;
	}

	/**
	 * For fuzzy extract, we will only ever extract one slot, since we're afraid of merging two item stacks with
	 * different damage values.
	 */
	@Override
	public ItemStack removeSimilarItems( int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination )
	{
		int slots = this.itemHandler.getSlots();
		ItemStack extracted = ItemStack.EMPTY;

		for( int slot = 0; slot < slots && extracted.isEmpty(); slot++ )
		{
			final ItemStack is = this.itemHandler.getStackInSlot( slot );
			if( is.isEmpty() || ( !filter.isEmpty() && !Platform.itemComparisons().isFuzzyEqualItem( is, filter, fuzzyMode ) ) )
			{
				continue;
			}

			if( destination != null )
			{
				ItemStack simulated = this.itemHandler.extractItem( slot, amount, true );
				if( simulated.isEmpty() )
				{
					continue;
				}

				if( !destination.canInsert( simulated ) )
				{
					continue;
				}
			}

			// Attempt extracting it
			extracted = this.itemHandler.extractItem( slot, amount, false );
		}

		return extracted;
	}

	@Override
	public ItemStack simulateSimilarRemove( int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination )
	{
		int slots = this.itemHandler.getSlots();
		ItemStack extracted = ItemStack.EMPTY;

		for( int slot = 0; slot < slots && extracted.isEmpty(); slot++ )
		{
			final ItemStack is = this.itemHandler.getStackInSlot( slot );
			if( is.isEmpty() || ( !filter.isEmpty() && !Platform.itemComparisons().isFuzzyEqualItem( is, filter, fuzzyMode ) ) )
			{
				continue;
			}

			// Attempt extracting it
			extracted = this.itemHandler.extractItem( slot, amount, true );

			if( !extracted.isEmpty() && destination != null )
			{
				if( !destination.canInsert( extracted ) )
				{
					extracted = ItemStack.EMPTY; // Keep on looking...
				}
			}
		}

		return extracted;
	}

	@Override
	public ItemStack addItems( ItemStack toBeAdded )
	{
		return this.addItems( toBeAdded, false );
	}

	@Override
	public ItemStack simulateAdd( ItemStack toBeSimulated )
	{
		return this.addItems( toBeSimulated, true );
	}

	protected ItemStack addItems( final ItemStack itemsToAdd, final boolean simulate )
	{
		if( itemsToAdd.isEmpty() )
		{
			return ItemStack.EMPTY;
		}

		ItemStack left = itemsToAdd.copy();

		for( int slot = 0; slot < this.itemHandler.getSlots(); slot++ )
		{
			left = this.itemHandler.insertItem( slot, left, simulate );

			if( left.isEmpty() )
			{
				return ItemStack.EMPTY;
			}
		}

		return left;
	}

	@Override
	public boolean containsItems()
	{
		int slots = this.itemHandler.getSlots();
		for( int slot = 0; slot < slots; slot++ )
		{
			if( !this.itemHandler.getStackInSlot( slot ).isEmpty() )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<ItemSlot> iterator()
	{
		return new ItemHandlerIterator( this.itemHandler );
	}

	@Override
	public boolean canRemoveAllExceptCircuits()
	{
		int slots = this.itemHandler.getSlots();
		for( int slot = 0; slot < slots; slot++ )
		{
			ItemStack is = this.itemHandler.getStackInSlot( slot );
			if ( is.isEmpty() || is.isItemEqual(integratedCircuit)) continue;
			return false;
		}
		return true;
	}

}
