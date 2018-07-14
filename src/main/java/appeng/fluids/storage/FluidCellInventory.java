/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.storage;


import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.fluids.util.AEFluidStack;
import appeng.me.storage.AbstractCellInventory;


/**
 * @author DrummerMC
 * @version rv6 - 2018-01-16
 * @since rv6 2018-01-16
 */
public class FluidCellInventory extends AbstractCellInventory<IAEFluidStack>
{
	protected FluidCellInventory( final NBTTagCompound data, final ISaveProvider container )
	{
		super( data, container, 8000 );
	}

	private FluidCellInventory( final ItemStack o, final ISaveProvider container ) throws AppEngException
	{
		super( o, container, 8000 );
	}

	public static IMEInventoryHandler<IAEFluidStack> getCell( final ItemStack o, final ISaveProvider container2 )
	{
		try
		{
			return new FluidCellInventoryHandler( new FluidCellInventory( o, container2 ) );
		}
		catch( final AppEngException e )
		{
			return null;
		}
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
			if( ( (IStorageCell) type ).getChannel() == AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) )
			{
				return ( (IStorageCell) type ).isStorageCell( i );
			}
		}

		return false;
	}

	@Override
	public IAEFluidStack injectItems( final IAEFluidStack input, final Actionable mode, final IActionSource src )
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

		final FluidStack sharedFluidStack = input.getFluidStack();

		final IAEFluidStack l = this.getCellItems().findPrecise( input );
		if( l != null )
		{
			final long remainingItemSlots = this.getRemainingItemCount();
			if( remainingItemSlots < 0 )
			{
				return input;
			}

			if( input.getStackSize() > remainingItemSlots )
			{
				final IAEFluidStack r = input.copy();
				r.setStackSize( r.getStackSize() - remainingItemSlots );
				if( mode == Actionable.MODULATE )
				{
					l.setStackSize( l.getStackSize() + remainingItemSlots );
					this.updateItemCount( remainingItemSlots );
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
					final FluidStack toReturn = sharedFluidStack.copy();
					toReturn.amount = sharedFluidStack.amount - remainingItemCount;
					if( mode == Actionable.MODULATE )
					{
						final FluidStack toWrite = sharedFluidStack.copy();
						toWrite.amount = remainingItemCount;

						this.cellItems.add( AEFluidStack.fromFluidStack( toWrite ) );
						this.updateItemCount( toWrite.amount );

						this.saveChanges();
					}
					return AEFluidStack.fromFluidStack( toReturn );
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
	public IAEFluidStack extractItems( final IAEFluidStack request, final Actionable mode, final IActionSource src )
	{
		if( request == null )
		{
			return null;
		}

		final long size = Math.min( Integer.MAX_VALUE, request.getStackSize() );

		IAEFluidStack results = null;

		final IAEFluidStack l = this.getCellItems().findPrecise( request );
		if( l != null )
		{
			results = l.copy();

			if( l.getStackSize() <= size )
			{
				results.setStackSize( l.getStackSize() );
				if( mode == Actionable.MODULATE )
				{
					this.updateItemCount( -l.getStackSize() );
					l.setStackSize( 0 );
					this.saveChanges();
				}
			}
			else
			{
				results.setStackSize( size );
				if( mode == Actionable.MODULATE )
				{
					l.setStackSize( l.getStackSize() - size );
					this.updateItemCount( -size );
					this.saveChanges();
				}
			}
		}

		return results;
	}

	@Override
	public IStorageChannel getChannel()
	{
		return AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class );
	}

	protected void loadCellItem( NBTTagCompound compoundTag, int stackSize )
	{

		// Now load the fluid stack
		final FluidStack t;
		try
		{
			t = FluidStack.loadFluidStackFromNBT( compoundTag );
			if( t == null )
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

		t.amount = stackSize;

		if( t.amount > 0 )
		{
			try
			{
				this.cellItems.add( AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ).createStack( t ) );
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
