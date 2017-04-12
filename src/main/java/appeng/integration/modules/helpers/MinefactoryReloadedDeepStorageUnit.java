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


import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;


public class MinefactoryReloadedDeepStorageUnit implements IMEInventory<IAEItemStack>
{

	private final IDeepStorageUnit dsu;

	public MinefactoryReloadedDeepStorageUnit( final TileEntity ta )
	{
		this.dsu = (IDeepStorageUnit) ta;
	}

	@Override
	public IAEItemStack injectItems( final IAEItemStack input, final Actionable mode, final BaseActionSource src )
	{
		final ItemStack is = this.dsu.getStoredItemType();
		if( is != null )
		{
			if( input.equals( is ) )
			{
				final long max = this.dsu.getMaxStoredCount();
				long storedItems = is.stackSize;
				if( max == storedItems )
				{
					return input;
				}

				storedItems += input.getStackSize();
				if( storedItems > max )
				{
					final IAEItemStack overflow = AEItemStack.create( is );
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
	public IAEItemStack extractItems( final IAEItemStack request, final Actionable mode, final BaseActionSource src )
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
	public IItemList<IAEItemStack> getAvailableItems( final IItemList<IAEItemStack> out )
	{
		final ItemStack is = this.dsu.getStoredItemType();
		if( is != null )
		{
			out.add( AEItemStack.create( is ) );
		}
		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}
}
