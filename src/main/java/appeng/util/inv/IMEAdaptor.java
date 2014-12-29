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

import com.google.common.collect.ImmutableList;

public class IMEAdaptor extends InventoryAdaptor
{

	final IMEInventory<IAEItemStack> target;
	final BaseActionSource src;
	int maxSlots = 0;

	public IMEAdaptor(IMEInventory<IAEItemStack> input, BaseActionSource src) {
		this.target = input;
		this.src = src;
	}

	IItemList<IAEItemStack> getList()
	{
		return this.target.getAvailableItems( AEApi.instance().storage().createItemList() );
	}

	@Override
	public Iterator<ItemSlot> iterator()
	{
		return new IMEAdaptorIterator( this, this.getList() );
	}

	public ItemStack doRemoveItemsFuzzy(int how_many, ItemStack Filter, IInventoryDestination destination, Actionable type, FuzzyMode fuzzyMode)
	{
		IAEItemStack reqFilter = AEItemStack.create( Filter );
		if ( reqFilter == null )
			return null;

		IAEItemStack out = null;

		for (IAEItemStack req : ImmutableList.copyOf( this.getList().findFuzzy( reqFilter, fuzzyMode ) ))
		{
			if ( req != null )
			{
				req.setStackSize( how_many );
				out = this.target.extractItems( req, type, this.src );
				if ( out != null )
					return out.getItemStack();
			}
		}

		return null;
	}

	public ItemStack doRemoveItems(int how_many, ItemStack Filter, IInventoryDestination destination, Actionable type)
	{
		IAEItemStack req = null;

		if ( Filter == null )
		{
			IItemList<IAEItemStack> list = this.getList();
			if ( !list.isEmpty() )
				req = list.getFirstItem();
		}
		else
			req = AEItemStack.create( Filter );

		IAEItemStack out = null;

		if ( req != null )
		{
			req.setStackSize( how_many );
			out = this.target.extractItems( req, type, this.src );
		}

		if ( out != null )
			return out.getItemStack();

		return null;
	}

	@Override
	public ItemStack removeItems(int how_many, ItemStack Filter, IInventoryDestination destination)
	{
		return this.doRemoveItems( how_many, Filter, destination, Actionable.MODULATE );
	}

	@Override
	public ItemStack simulateRemove(int how_many, ItemStack Filter, IInventoryDestination destination)
	{
		return this.doRemoveItems( how_many, Filter, destination, Actionable.SIMULATE );
	}

	@Override
	public ItemStack removeSimilarItems(int how_many, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination)
	{
		if ( filter == null )
			return this.doRemoveItems( how_many, null, destination, Actionable.MODULATE );
		return this.doRemoveItemsFuzzy( how_many, filter, destination, Actionable.MODULATE, fuzzyMode );
	}

	@Override
	public ItemStack simulateSimilarRemove(int how_many, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination)
	{
		if ( filter == null )
			return this.doRemoveItems( how_many, null, destination, Actionable.SIMULATE );
		return this.doRemoveItemsFuzzy( how_many, filter, destination, Actionable.SIMULATE, fuzzyMode );
	}

	@Override
	public ItemStack addItems(ItemStack A)
	{
		IAEItemStack in = AEItemStack.create( A );
		if ( in != null )
		{
			IAEItemStack out = this.target.injectItems( in, Actionable.MODULATE, this.src );
			if ( out != null )
				return out.getItemStack();
		}
		return null;
	}

	@Override
	public ItemStack simulateAdd(ItemStack A)
	{
		IAEItemStack in = AEItemStack.create( A );
		if ( in != null )
		{
			IAEItemStack out = this.target.injectItems( in, Actionable.SIMULATE, this.src );
			if ( out != null )
				return out.getItemStack();
		}
		return null;
	}

	@Override
	public boolean containsItems()
	{
		return !this.getList().isEmpty();
	}

}
