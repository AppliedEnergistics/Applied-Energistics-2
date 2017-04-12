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

package appeng.integration.modules.helpers;


import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.ItemSlot;
import appeng.util.iterators.StackToSlotIterator;
import net.mcft.copy.betterstorage.api.crate.ICrateStorage;
import net.minecraft.item.ItemStack;

import java.util.Iterator;


public class BSCrateStorageAdaptor extends InventoryAdaptor
{

	private final ICrateStorage cs;

	public BSCrateStorageAdaptor( final Object te )
	{
		this.cs = (ICrateStorage) te;
	}

	@Override
	public ItemStack removeItems( final int amount, final ItemStack filter, final IInventoryDestination destination )
	{
		ItemStack target = null;

		for( final ItemStack is : this.cs.getContents() )
		{
			if( is != null )
			{
				if( is.stackSize > 0 && ( filter == null || Platform.isSameItem( filter, is ) ) )
				{
					if( destination == null || destination.canInsert( is ) )
					{
						target = is;
						break;
					}
				}
			}
		}

		if( target != null )
		{
			final ItemStack f = Platform.cloneItemStack( target );
			f.stackSize = amount;
			return this.cs.extractItems( f, amount );
		}

		return null;
	}

	@Override
	public ItemStack simulateRemove( final int amount, final ItemStack filter, final IInventoryDestination destination )
	{
		ItemStack target = null;

		for( final ItemStack is : this.cs.getContents() )
		{
			if( is != null )
			{
				if( is.stackSize > 0 && ( filter == null || Platform.isSameItem( filter, is ) ) )
				{
					if( destination == null || destination.canInsert( is ) )
					{
						target = is;
						break;
					}
				}
			}
		}

		if( target != null )
		{
			int cnt = this.cs.getItemCount( target );
			if( cnt == 0 )
			{
				return null;
			}
			if( cnt > amount )
			{
				cnt = amount;
			}
			final ItemStack c = target.copy();
			c.stackSize = cnt;
			return c;
		}

		return null;
	}

	@Override
	public ItemStack removeSimilarItems( final int amount, final ItemStack filter, final FuzzyMode fuzzyMode, final IInventoryDestination destination )
	{
		ItemStack target = null;

		for( final ItemStack is : this.cs.getContents() )
		{
			if( is != null )
			{
				if( is.stackSize > 0 && ( filter == null || Platform.isSameItemFuzzy( filter, is, fuzzyMode ) ) )
				{
					if( destination == null || destination.canInsert( is ) )
					{
						target = is;
						break;
					}
				}
			}
		}

		if( target != null )
		{
			final ItemStack f = Platform.cloneItemStack( target );
			f.stackSize = amount;
			return this.cs.extractItems( f, amount );
		}

		return null;
	}

	@Override
	public ItemStack simulateSimilarRemove( final int amount, final ItemStack filter, final FuzzyMode fuzzyMode, final IInventoryDestination destination )
	{
		ItemStack target = null;

		for( final ItemStack is : this.cs.getContents() )
		{
			if( is != null )
			{
				if( is.stackSize > 0 && ( filter == null || Platform.isSameItemFuzzy( filter, is, fuzzyMode ) ) )
				{
					if( destination == null || destination.canInsert( is ) )
					{
						target = is;
						break;
					}
				}
			}
		}

		if( target != null )
		{
			int cnt = this.cs.getItemCount( target );
			if( cnt == 0 )
			{
				return null;
			}
			if( cnt > amount )
			{
				cnt = amount;
			}
			final ItemStack c = target.copy();
			c.stackSize = cnt;
			return c;
		}

		return null;
	}

	@Override
	public ItemStack addItems( final ItemStack toBeAdded )
	{
		return this.cs.insertItems( toBeAdded );
	}

	@Override
	public ItemStack simulateAdd( final ItemStack toBeSimulated )
	{
		final int items = this.cs.getSpaceForItem( toBeSimulated );
		final ItemStack cloned = Platform.cloneItemStack( toBeSimulated );
		if( toBeSimulated.stackSize <= items )
		{
			return null;
		}
		cloned.stackSize -= items;
		return cloned;
	}

	@Override
	public boolean containsItems()
	{
		return this.cs.getUniqueItems() > 0;
	}

	@Override
	public Iterator<ItemSlot> iterator()
	{
		return new StackToSlotIterator( this.cs.getContents().iterator() );
	}
}
