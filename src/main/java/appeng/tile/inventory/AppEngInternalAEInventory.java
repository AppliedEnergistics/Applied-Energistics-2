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

package appeng.tile.inventory;


import java.util.Iterator;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemHandlerHelper;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AELog;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.IInternalItemHandler;
import appeng.util.inv.InvOperation;
import appeng.util.item.AEItemStack;
import appeng.util.iterators.AEInvIterator;
import appeng.util.iterators.InvIterator;


public class AppEngInternalAEInventory implements IInternalItemHandler, Iterable<ItemStack>
{
	private final IAEAppEngInventory te;
	private final IAEItemStack[] inv;
	private final int size;
	private int maxStack;

	public AppEngInternalAEInventory( final IAEAppEngInventory te, final int s )
	{
		this.te = te;
		this.size = s;
		this.maxStack = 64;
		this.inv = new IAEItemStack[s];
	}

	public void setMaxStackSize( final int s )
	{
		this.maxStack = s;
	}

	public IAEItemStack getAEStackInSlot( final int var1 )
	{
		return this.inv[var1];
	}

	public void writeToNBT( final NBTTagCompound data, final String name )
	{
		final NBTTagCompound c = new NBTTagCompound();
		this.writeToNBT( c );
		data.setTag( name, c );
	}

	private void writeToNBT( final NBTTagCompound target )
	{
		for( int x = 0; x < this.size; x++ )
		{
			try
			{
				final NBTTagCompound c = new NBTTagCompound();

				if( this.inv[x] != null )
				{
					this.inv[x].writeToNBT( c );
				}

				target.setTag( "#" + x, c );
			}
			catch( final Exception ignored )
			{
			}
		}
	}

	public void readFromNBT( final NBTTagCompound data, final String name )
	{
		final NBTTagCompound c = data.getCompoundTag( name );
		if( c != null )
		{
			this.readFromNBT( c );
		}
	}

	private void readFromNBT( final NBTTagCompound target )
	{
		for( int x = 0; x < this.size; x++ )
		{
			try
			{
				final NBTTagCompound c = target.getCompoundTag( "#" + x );

				if( c != null )
				{
					this.inv[x] = AEItemStack.loadItemStackFromNBT( c );
				}
			}
			catch( final Exception e )
			{
				AELog.debug( e );
			}
		}
	}

	protected int getStackLimit( int slot, @Nonnull ItemStack stack )
	{
		return Math.min( getSlotLimit( slot ), stack.getMaxStackSize() );
	}

	@Override
	public int getSlots()
	{
		return this.size;
	}

	@Override
	public ItemStack getStackInSlot( final int var1 )
	{
		if( this.inv[var1] == null )
		{
			return ItemStack.EMPTY;
		}

		return this.inv[var1].getItemStack();
	}

	@Override
	public ItemStack insertItem( int slot, ItemStack stack, boolean simulate )
	{
		if( stack.isEmpty() )
		{
			return ItemStack.EMPTY;
		}

		ItemStack existing = getStackInSlot( slot );
		int limit = getStackLimit( slot, stack );

		if( !existing.isEmpty() )
		{
			if( !ItemHandlerHelper.canItemStacksStack( stack, existing ) )
			{
				return stack;
			}

			limit -= existing.getCount();
		}

		if( limit <= 0 )
		{
			return stack;
		}

		boolean reachedLimit = stack.getCount() > limit;

		if( !simulate )
		{
			if( existing.isEmpty() )
			{
				this.inv[slot] = AEApi.instance().storage().createItemStack( reachedLimit ? ItemHandlerHelper.copyStackWithSize( stack, limit ) : stack );
			}
			else
			{
				existing.grow( reachedLimit ? limit : stack.getCount() );
			}
			fireOnChangeInventory( slot, InvOperation.INSERT, ItemStack.EMPTY, reachedLimit ? ItemHandlerHelper.copyStackWithSize( stack, limit ) : stack );
		}
		return reachedLimit ? ItemHandlerHelper.copyStackWithSize( stack, stack.getCount() - limit ) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem( int slot, int amount, boolean simulate )
	{
		if( this.inv[slot] != null )
		{
			final ItemStack split = this.getStackInSlot( slot );

			if( amount >= split.getCount() )
			{
				if( !simulate )
				{
					this.inv[slot] = null;
					fireOnChangeInventory( slot, InvOperation.EXTRACT, split, ItemStack.EMPTY );
				}
				return split;
			}
			else
			{
				if( !simulate )
				{
					split.grow( -amount );
					fireOnChangeInventory( slot, InvOperation.EXTRACT, ItemHandlerHelper.copyStackWithSize( split, amount ), ItemStack.EMPTY );
				}
				return ItemHandlerHelper.copyStackWithSize( split, amount );
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setStackInSlot( final int slot, final ItemStack newItemStack )
	{
		final ItemStack oldStack = this.getStackInSlot( slot );
		this.inv[slot] = AEApi.instance().storage().createItemStack( newItemStack );

		if( this.te != null && Platform.isServer() )
		{
			ItemStack removed = oldStack;
			ItemStack added = newItemStack;

			if( !oldStack.isEmpty() && !newItemStack.isEmpty() && Platform.itemComparisons().isEqualItem( oldStack, newItemStack ) )
			{
				if( oldStack.getCount() > newItemStack.getCount() )
				{
					removed = removed.copy();
					removed.grow( -newItemStack.getCount() );
					added = ItemStack.EMPTY;
				}
				else if( oldStack.getCount() < newItemStack.getCount() )
				{
					added = added.copy();
					added.grow( -oldStack.getCount() );
					removed = ItemStack.EMPTY;
				}
				else
				{
					removed = added = ItemStack.EMPTY;
				}
			}
			fireOnChangeInventory( slot, InvOperation.SET, removed, added );
		}
	}

	private void fireOnChangeInventory( int slot, InvOperation op, ItemStack removed, ItemStack inserted )
	{
		if( this.te != null && Platform.isServer() )
		{
			this.te.onChangeInventory( this, slot, op, removed, inserted );
		}
	}

	@Override
	public int getSlotLimit( int slot )
	{
		return this.maxStack > 64 ? 64 : this.maxStack;
	}

	@Override
	public Iterator<ItemStack> iterator()
	{
		return new InvIterator( this );
	}

	public Iterator<IAEItemStack> getNewAEIterator()
	{
		return new AEInvIterator( this );
	}

	@Override
	public boolean isItemValidForSlot( int slot, ItemStack stack )
	{
		return true;
	}

	@Override
	public void markDirty( int slot )
	{
		fireOnChangeInventory( slot, InvOperation.DIRTY, ItemStack.EMPTY, ItemStack.EMPTY );
	}
}
