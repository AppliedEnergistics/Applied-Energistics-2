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


import java.util.Iterator;

import net.mcft.copy.betterstorage.api.crate.ICrateStorage;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.ItemSlot;
import appeng.util.iterators.StackToSlotIterator;


public class BSCrateStorageAdaptor extends InventoryAdaptor
{

	final ICrateStorage cs;
	final ForgeDirection side;

	public BSCrateStorageAdaptor( Object te, ForgeDirection d )
	{
		this.cs = (ICrateStorage) te;
		this.side = d;
	}

	@Override
	public ItemStack removeItems( int amount, ItemStack filter, IInventoryDestination destination )
	{
		ItemStack target = null;

		for( ItemStack is : this.cs.getContents() )
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
			ItemStack f = Platform.cloneItemStack( target );
			f.stackSize = amount;
			return cs.extractItems( f, amount );
		}

		return null;
	}

	@Override
	public ItemStack simulateRemove( int amount, ItemStack filter, IInventoryDestination destination )
	{
		ItemStack target = null;

		for( ItemStack is : this.cs.getContents() )
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
				return null;
			if( cnt > amount )
				cnt = amount;
			ItemStack c = target.copy();
			c.stackSize = cnt;
			return c;
		}

		return null;
	}

	@Override
	public ItemStack removeSimilarItems( int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination )
	{
		ItemStack target = null;

		for( ItemStack is : this.cs.getContents() )
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
			ItemStack f = Platform.cloneItemStack( target );
			f.stackSize = amount;
			return this.cs.extractItems( f, amount );
		}

		return null;
	}

	@Override
	public ItemStack simulateSimilarRemove( int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination )
	{
		ItemStack target = null;

		for( ItemStack is : this.cs.getContents() )
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
				return null;
			if( cnt > amount )
				cnt = amount;
			ItemStack c = target.copy();
			c.stackSize = cnt;
			return c;
		}

		return null;
	}

	@Override
	public ItemStack addItems( ItemStack toBeAdded )
	{
		return this.cs.insertItems( toBeAdded );
	}

	@Override
	public ItemStack simulateAdd( ItemStack toBeSimulated )
	{
		int items = this.cs.getSpaceForItem( toBeSimulated );
		ItemStack B = Platform.cloneItemStack( toBeSimulated );
		if( toBeSimulated.stackSize <= items )
			return null;
		B.stackSize -= items;
		return B;
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
