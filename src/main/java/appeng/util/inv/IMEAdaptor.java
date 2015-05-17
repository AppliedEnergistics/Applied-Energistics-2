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

import com.google.common.collect.ImmutableList;

import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.InventoryAdaptor;
import appeng.util.item.AEItemStack;


public final class IMEAdaptor extends InventoryAdaptor
{

	final IMEInventory<IAEItemStack> target;
	final BaseActionSource src;
	int maxSlots = 0;

	public IMEAdaptor( IMEInventory<IAEItemStack> input, BaseActionSource src )
	{
		this.target = input;
		this.src = src;
	}

	@Override
	public final Iterator<ItemSlot> iterator()
	{
		return new IMEAdaptorIterator( this, this.getList() );
	}

	final IItemList<IAEItemStack> getList()
	{
		return this.target.getAvailableItems( AEApi.instance().storage().createItemList() );
	}

	@Override
	public ItemStack removeItems( int amount, ItemStack filter, IInventoryDestination destination )
	{
		return this.doRemoveItems( amount, filter, destination, Actionable.MODULATE );
	}

	public final ItemStack doRemoveItems( int amount, ItemStack filter, IInventoryDestination destination, Actionable type )
	{
		IAEItemStack req = null;

		if( filter == null )
		{
			IItemList<IAEItemStack> list = this.getList();
			if( !list.isEmpty() )
			{
				req = list.getFirstItem();
			}
		}
		else
		{
			req = AEItemStack.create( filter );
		}

		IAEItemStack out = null;

		if( req != null )
		{
			req.setStackSize( amount );
			out = this.target.extractItems( req, type, this.src );
		}

		if( out != null )
		{
			return out.getItemStack();
		}

		return null;
	}

	@Override
	public ItemStack simulateRemove( int amount, ItemStack filter, IInventoryDestination destination )
	{
		return this.doRemoveItems( amount, filter, destination, Actionable.SIMULATE );
	}

	@Override
	public ItemStack removeSimilarItems( int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination )
	{
		if( filter == null )
		{
			return this.doRemoveItems( amount, null, destination, Actionable.MODULATE );
		}
		return this.doRemoveItemsFuzzy( amount, filter, destination, Actionable.MODULATE, fuzzyMode );
	}

	public final ItemStack doRemoveItemsFuzzy( int amount, ItemStack filter, IInventoryDestination destination, Actionable type, FuzzyMode fuzzyMode )
	{
		IAEItemStack reqFilter = AEItemStack.create( filter );
		if( reqFilter == null )
		{
			return null;
		}

		IAEItemStack out = null;

		for( IAEItemStack req : ImmutableList.copyOf( this.getList().findFuzzy( reqFilter, fuzzyMode ) ) )
		{
			if( req != null )
			{
				req.setStackSize( amount );
				out = this.target.extractItems( req, type, this.src );
				if( out != null )
				{
					return out.getItemStack();
				}
			}
		}

		return null;
	}

	@Override
	public ItemStack simulateSimilarRemove( int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination )
	{
		if( filter == null )
		{
			return this.doRemoveItems( amount, null, destination, Actionable.SIMULATE );
		}
		return this.doRemoveItemsFuzzy( amount, filter, destination, Actionable.SIMULATE, fuzzyMode );
	}

	@Override
	public ItemStack addItems( ItemStack toBeAdded )
	{
		IAEItemStack in = AEItemStack.create( toBeAdded );
		if( in != null )
		{
			IAEItemStack out = this.target.injectItems( in, Actionable.MODULATE, this.src );
			if( out != null )
			{
				return out.getItemStack();
			}
		}
		return null;
	}

	@Override
	public ItemStack simulateAdd( ItemStack toBeSimulated )
	{
		IAEItemStack in = AEItemStack.create( toBeSimulated );
		if( in != null )
		{
			IAEItemStack out = this.target.injectItems( in, Actionable.SIMULATE, this.src );
			if( out != null )
			{
				return out.getItemStack();
			}
		}
		return null;
	}

	@Override
	public boolean containsItems()
	{
		return !this.getList().isEmpty();
	}
}
