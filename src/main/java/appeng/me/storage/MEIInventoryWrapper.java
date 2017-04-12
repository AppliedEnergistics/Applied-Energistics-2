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

package appeng.me.storage;


import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;


public class MEIInventoryWrapper implements IMEInventory<IAEItemStack>
{

	private final IInventory target;
	private final InventoryAdaptor adaptor;

	public MEIInventoryWrapper( final IInventory m, final InventoryAdaptor ia )
	{
		this.target = m;
		this.adaptor = ia;
	}

	@Override
	public IAEItemStack injectItems( final IAEItemStack iox, final Actionable mode, final BaseActionSource src )
	{
		final ItemStack input = iox.getItemStack();

		if( this.adaptor != null )
		{
			final ItemStack is = mode == Actionable.SIMULATE ? this.adaptor.simulateAdd( input ) : this.adaptor.addItems( input );
			if( is == null )
			{
				return null;
			}
			return AEItemStack.create( is );
		}

		final ItemStack out = Platform.cloneItemStack( input );

		if( mode == Actionable.MODULATE ) // absolutely no need for a first run in simulate mode.
		{
			for( int x = 0; x < this.target.getSizeInventory(); x++ )
			{
				final ItemStack t = this.target.getStackInSlot( x );

				if( Platform.isSameItem( t, input ) )
				{
					final int oriStack = t.stackSize;
					t.stackSize += out.stackSize;

					this.target.setInventorySlotContents( x, t );

					if( t.stackSize > this.target.getInventoryStackLimit() )
					{
						t.stackSize = this.target.getInventoryStackLimit();
					}

					if( t.stackSize > t.getMaxStackSize() )
					{
						t.stackSize = t.getMaxStackSize();
					}

					out.stackSize -= t.stackSize - oriStack;

					if( out.stackSize <= 0 )
					{
						return null;
					}
				}
			}
		}

		for( int x = 0; x < this.target.getSizeInventory(); x++ )
		{
			ItemStack t = this.target.getStackInSlot( x );

			if( t == null )
			{
				t = Platform.cloneItemStack( input );
				t.stackSize = out.stackSize;

				if( t.stackSize > this.target.getInventoryStackLimit() )
				{
					t.stackSize = this.target.getInventoryStackLimit();
				}

				out.stackSize -= t.stackSize;
				if( mode == Actionable.MODULATE )
				{
					this.target.setInventorySlotContents( x, t );
				}

				if( out.stackSize <= 0 )
				{
					return null;
				}
			}
		}

		return AEItemStack.create( out );
	}

	@Override
	public IAEItemStack extractItems( final IAEItemStack request, final Actionable mode, final BaseActionSource src )
	{
		final ItemStack Req = request.getItemStack();

		int request_stackSize = Req.stackSize;

		if( request_stackSize > Req.getMaxStackSize() )
		{
			request_stackSize = Req.getMaxStackSize();
		}

		Req.stackSize = request_stackSize;

		ItemStack Gathered = null;
		if( this.adaptor != null )
		{
			Gathered = this.adaptor.removeItems( Req.stackSize, Req, null );
		}
		else
		{
			Gathered = request.getItemStack();
			Gathered.stackSize = 0;

			// try to find matching inventories that already have it...
			for( int x = 0; x < this.target.getSizeInventory(); x++ )
			{
				final ItemStack sub = this.target.getStackInSlot( x );

				if( Platform.isSameItem( sub, Req ) )
				{
					int reqNum = Req.stackSize;

					if( reqNum > sub.stackSize )
					{
						reqNum = Req.stackSize;
					}

					ItemStack retrieved = null;

					if( sub.stackSize < Req.stackSize )
					{
						retrieved = Platform.cloneItemStack( sub );
						sub.stackSize = 0;
					}
					else
					{
						retrieved = sub.splitStack( Req.stackSize );
					}

					if( sub.stackSize <= 0 )
					{
						this.target.setInventorySlotContents( x, null );
					}
					else
					{
						this.target.setInventorySlotContents( x, sub );
					}

					if( retrieved != null )
					{
						Gathered.stackSize += retrieved.stackSize;
						Req.stackSize -= retrieved.stackSize;
					}

					if( request_stackSize == Gathered.stackSize )
					{
						return AEItemStack.create( Gathered );
					}
				}
			}

			if( Gathered.stackSize == 0 )
			{
				return null;
			}
		}

		return AEItemStack.create( Gathered );
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems( final IItemList<IAEItemStack> out )
	{
		for( int x = 0; x < this.target.getSizeInventory(); x++ )
		{
			out.addStorage( AEItemStack.create( this.target.getStackInSlot( x ) ) );
		}

		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}
}
