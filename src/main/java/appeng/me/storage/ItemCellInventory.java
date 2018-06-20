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


import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AEConfig;
import appeng.core.AELog;


public class ItemCellInventory extends AbstractCellInventory<IAEItemStack>
{
	protected ItemCellInventory( final NBTTagCompound data, final ISaveProvider container )
	{
		super( data, container, 8 );
	}

	private ItemCellInventory( final ItemStack o, final ISaveProvider container ) throws AppEngException
	{
		super( o, container, 8 );
	}

	public static IMEInventoryHandler getCell( final ItemStack o, final ISaveProvider container2 )
	{
		try
		{
			return new ItemCellInventoryHandler( new ItemCellInventory( o, container2 ) );
		}
		catch( final AppEngException e )
		{
			return null;
		}
	}

	private static boolean isStorageCell( final ItemStack i )
	{
		if( i == null )
		{
			return false;
		}

		try
		{
			final Item type = i.getItem();
			if( type instanceof IStorageCell )
			{
				return !( (IStorageCell) type ).storableInStorageCell();
			}
		}
		catch( final Throwable err )
		{
			return true;
		}

		return false;
	}

	public static boolean isCell( final ItemStack i )
	{
		if( i == null )
		{
			return false;
		}

		final Item type = i.getItem();
		if( type instanceof IStorageCell )
		{
			if ( ( (IStorageCell) type ).getChannel() == AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) )
			{
				return ( (IStorageCell) type ).isStorageCell( i );
			}
		}

		return false;
	}

	@Override
	public IAEItemStack injectItems( final IAEItemStack input, final Actionable mode, final IActionSource src )
	{
		if( input == null )
		{
			return null;
		}
		if( input.getStackSize() == 0 )
		{
			return null;
		}

		if( this.cellType.isBlackListed( this.i, input ) )
		{
			return input;
		}
		// This is slightly hacky as it expects a read-only access, but fine for now.
		// TODO: Guarantee a read-only access. E.g. provide an isEmpty() method and ensure CellInventory does not write
		// any NBT data for empty cells instead of relying on an empty IItemContainer
		if( ItemCellInventory.isStorageCell( input.getDefinition() ) )
		{
			final IMEInventory meInventory = getCell( input.createItemStack(), null );
			if( meInventory != null && !this.isEmpty( meInventory ) )
			{
				return input;
			}
		}

		final IAEItemStack l = this.getCellItems().findPrecise( input );
		if( l != null )
		{
			final long remainingItemCount = this.getRemainingItemCount();
			if( remainingItemCount < 0 )
			{
				return input;
			}

			if( input.getStackSize() > remainingItemCount )
			{
				final IAEItemStack r = input.copy();
				r.setStackSize( r.getStackSize() - remainingItemCount );
				if( mode == Actionable.MODULATE )
				{
					l.setStackSize( l.getStackSize() + remainingItemCount );
					this.updateItemCount( remainingItemCount );
					this.saveChanges();
				}
				return r;
			}
			else
			{
				if( mode == Actionable.MODULATE )
				{
					l.setStackSize( l.getStackSize() + input.getStackSize() );
					this.updateItemCount( input.getStackSize() );
					this.saveChanges();
				}
				return null;
			}
		}

		if( this.canHoldNewItem() ) // room for new type, and for at least one item!
		{
			final int remainingItemCount = (int) this.getRemainingItemCount() - this.getBytesPerType() * itemsPerByte;
			if( remainingItemCount > 0 )
			{
				if( input.getStackSize() > remainingItemCount )
				{
					final IAEItemStack toReturn = input.copy();
					toReturn.setStackSize( input.getStackSize() - remainingItemCount );
					if( mode == Actionable.MODULATE )
					{
						final IAEItemStack toWrite = input.copy();
						toWrite.setStackSize( remainingItemCount );

						this.cellItems.add( toWrite );
						this.updateItemCount( toWrite.getStackSize() );

						this.saveChanges();
					}
					return toReturn;
				}

				if( mode == Actionable.MODULATE )
				{
					this.updateItemCount( input.getStackSize() );
					this.cellItems.add( input );
					this.saveChanges();
				}

				return null;
			}
		}

		return input;
	}

	@Override
	public IAEItemStack extractItems( final IAEItemStack request, final Actionable mode, final IActionSource src )
	{
		if( request == null )
		{
			return null;
		}

		final long size = Math.min( Integer.MAX_VALUE, request.getStackSize() );

		IAEItemStack Results = null;

		final IAEItemStack l = this.getCellItems().findPrecise( request );
		if( l != null )
		{
			Results = l.copy();

			if( l.getStackSize() <= size )
			{
				Results.setStackSize( l.getStackSize() );
				if( mode == Actionable.MODULATE )
				{
					this.updateItemCount( -l.getStackSize() );
					l.setStackSize( 0 );
					this.saveChanges();
				}
			}
			else
			{
				Results.setStackSize( size );
				if( mode == Actionable.MODULATE )
				{
					l.setStackSize( l.getStackSize() - size );
					this.updateItemCount( -size );
					this.saveChanges();
				}
			}
		}

		return Results;
	}

	@Override
	public IStorageChannel getChannel()
	{
		return AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class );
	}

	protected void loadCellItem( NBTTagCompound compoundTag, int stackSize )
	{

		// Now load the item stack
		final ItemStack t;
		try
		{
			t = new ItemStack( compoundTag );
			if( t.isEmpty() )
			{
				AELog.warn( "Removing item " + compoundTag + " from storage cell because the associated item type couldn't be found." );
				return;
			}
		}
		catch( Throwable ex )
		{
			if( AEConfig.instance().isRemoveCrashingItemsOnLoad() )
			{
				AELog.warn( ex, "Removing item " + compoundTag + " from storage cell because loading the ItemStack crashed." );
				return;
			}
			throw ex;
		}

		t.setCount( stackSize );

		if( t.getCount() > 0 )
		{
			try
			{
				this.cellItems.add( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createStack( t ) );
			}
			catch( Throwable ex )
			{
				if( AEConfig.instance().isRemoveCrashingItemsOnLoad() )
				{
					AELog.warn( ex, "Removing item " + t + " from storage cell because processing the loaded item crashed." );
					return;
				}
				throw ex;
			}
		}
	}
}
