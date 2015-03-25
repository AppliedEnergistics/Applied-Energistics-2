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


import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.integration.abstraction.IFZ;
import appeng.util.item.AEItemStack;


public class FactorizationBarrel implements IMEInventory<IAEItemStack>
{

	final IFZ fProxy;
	private final TileEntity te;

	public FactorizationBarrel( IFZ proxy, TileEntity tile )
	{
		this.te = tile;
		this.fProxy = proxy;
	}

	public long remainingItemCount()
	{
		return this.fProxy.barrelGetMaxItemCount( this.te ) - this.fProxy.barrelGetItemCount( this.te );
	}

	@Override
	public IAEItemStack injectItems( IAEItemStack input, Actionable mode, BaseActionSource src )
	{
		if( input == null )
			return null;
		if( input.getStackSize() == 0 )
			return null;

		ItemStack shared = input.getItemStack();
		if( shared.isItemDamaged() )
			return input;

		if( this.remainingItemTypes() > 0 )
		{
			if( mode == Actionable.MODULATE )
				this.fProxy.setItemType( this.te, input.getItemStack() );
		}

		if( this.containsItemType( input, mode == Actionable.SIMULATE ) )
		{
			int max = this.fProxy.barrelGetMaxItemCount( this.te );
			int newTotal = (int) this.storedItemCount() + (int) input.getStackSize();
			if( newTotal > max )
			{
				if( mode == Actionable.MODULATE )
					this.fProxy.barrelSetCount( this.te, max );
				IAEItemStack result = input.copy();
				result.setStackSize( newTotal - max );
				return result;
			}
			else
			{
				if( mode == Actionable.MODULATE )
					this.fProxy.barrelSetCount( this.te, newTotal );
				return null;
			}
		}

		return input;
	}

	public long remainingItemTypes()
	{
		return this.fProxy.barrelGetItem( this.te ) == null ? 1 : 0;
	}

	public boolean containsItemType( IAEItemStack i, boolean acceptEmpty )
	{
		ItemStack currentItem = this.fProxy.barrelGetItem( this.te );

		// empty barrels want your love too!
		if( acceptEmpty && currentItem == null )
			return true;

		return i.equals( currentItem );
	}

	public long storedItemCount()
	{
		return this.fProxy.barrelGetItemCount( this.te );
	}

	@Override
	public IAEItemStack extractItems( IAEItemStack request, Actionable mode, BaseActionSource src )
	{
		if( this.containsItemType( request, false ) )
		{
			int howMany = (int) this.storedItemCount();
			if( request.getStackSize() >= howMany )
			{
				if( mode == Actionable.MODULATE )
				{
					this.fProxy.setItemType( this.te, null );
					this.fProxy.barrelSetCount( this.te, 0 );
				}

				IAEItemStack r = request.copy();
				r.setStackSize( howMany );
				return r;
			}
			else
			{
				if( mode == Actionable.MODULATE )
					this.fProxy.barrelSetCount( this.te, (int) ( howMany - request.getStackSize() ) );
				return request.copy();
			}
		}
		return null;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems( IItemList out )
	{
		ItemStack i = this.fProxy.barrelGetItem( this.te );
		if( i != null )
		{
			i.stackSize = this.fProxy.barrelGetItemCount( this.te );
			out.addStorage( AEItemStack.create( i ) );
		}

		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}
}