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

package appeng.integration.modules.helpers;


import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;


public final class MinefactoryReloadedDeepStorageUnit implements IMEInventory<IAEItemStack>
{

	final IDeepStorageUnit dsu;
	final TileEntity te;

	public MinefactoryReloadedDeepStorageUnit( TileEntity ta )
	{
		this.te = ta;
		this.dsu = (IDeepStorageUnit) ta;
	}

	@Override
	public final IAEItemStack injectItems( IAEItemStack input, Actionable mode, BaseActionSource src )
	{
		ItemStack is = this.dsu.getStoredItemType();
		if( is != null )
		{
			if( input.equals( is ) )
			{
				long max = this.dsu.getMaxStoredCount();
				long storedItems = is.stackSize;
				if( max == storedItems )
				{
					return input;
				}

				storedItems += input.getStackSize();
				if( storedItems > max )
				{
					IAEItemStack overflow = AEItemStack.create( is );
					overflow.setStackSize( (int) ( storedItems - max ) );
					if( mode == Actionable.MODULATE )
					{
						this.dsu.setStoredItemCount( (int) max );
					}
					return overflow;
				}
				else
				{
					if( mode == Actionable.MODULATE )
					{
						this.dsu.setStoredItemCount( is.stackSize + (int) input.getStackSize() );
					}
					return null;
				}
			}
		}
		else
		{
			if( input.getTagCompound() != null )
			{
				return input;
			}
			if( mode == Actionable.MODULATE )
			{
				this.dsu.setStoredItemType( input.getItemStack(), (int) input.getStackSize() );
			}
			return null;
		}
		return input;
	}

	@Override
	public final IAEItemStack extractItems( IAEItemStack request, Actionable mode, BaseActionSource src )
	{
		ItemStack is = this.dsu.getStoredItemType();
		if( request.equals( is ) )
		{
			if( request.getStackSize() >= is.stackSize )
			{
				is = is.copy();
				if( mode == Actionable.MODULATE )
				{
					this.dsu.setStoredItemCount( 0 );
				}
				return AEItemStack.create( is );
			}
			else
			{
				if( mode == Actionable.MODULATE )
				{
					this.dsu.setStoredItemCount( is.stackSize - (int) request.getStackSize() );
				}
				return request.copy();
			}
		}
		return null;
	}

	@Override
	public final IItemList<IAEItemStack> getAvailableItems( IItemList<IAEItemStack> out )
	{
		ItemStack is = this.dsu.getStoredItemType();
		if( is != null )
		{
			out.add( AEItemStack.create( is ) );
		}
		return out;
	}

	@Override
	public final StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}
}
