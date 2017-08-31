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


import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AELog;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.iterators.AEInvIterator;
import appeng.util.iterators.InvIterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Iterator;


public class AppEngInternalAEInventory implements IInventory, Iterable<ItemStack>
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

	public boolean isEmpty()
	{
		for( int x = 0; x < this.size; x++ )
		{
			if( this.getStackInSlot( x ) != null )
			{
				return false;
			}
		}
		return true;
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

	@Override
	public int getSizeInventory()
	{
		return this.size;
	}

	@Override
	public ItemStack getStackInSlot( final int var1 )
	{
		if( this.inv[var1] == null )
		{
			return null;
		}

		return this.inv[var1].getItemStack();
	}

	@Override
	public ItemStack decrStackSize( final int slot, final int qty )
	{
		if( this.inv[slot] != null )
		{
			final ItemStack split = this.getStackInSlot( slot );
			ItemStack ns = null;

			if( qty >= split.stackSize )
			{
				ns = this.getStackInSlot( slot );
				this.inv[slot] = null;
			}
			else
			{
				ns = split.splitStack( qty );
			}

			if( this.te != null && Platform.isServer() )
			{
				this.te.onChangeInventory( this, slot, InvOperation.decreaseStackSize, ns, null );
				this.te.saveChanges();
			}

			return ns;
		}

		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing( final int var1 )
	{
		return null;
	}

	@Override
	public void setInventorySlotContents( final int slot, final ItemStack newItemStack )
	{
		final ItemStack oldStack = this.getStackInSlot( slot );
		this.inv[slot] = AEApi.instance().storage().createItemStack( newItemStack );

		if( this.te != null && Platform.isServer() )
		{
			ItemStack removed = oldStack;
			ItemStack added = newItemStack;

			if( oldStack != null && newItemStack != null && Platform.isSameItem( oldStack, newItemStack ) )
			{
				if( oldStack.stackSize > newItemStack.stackSize )
				{
					removed = removed.copy();
					removed.stackSize -= newItemStack.stackSize;
					added = null;
				}
				else if( oldStack.stackSize < newItemStack.stackSize )
				{
					added = added.copy();
					added.stackSize -= oldStack.stackSize;
					removed = null;
				}
				else
				{
					removed = added = null;
				}
			}

			this.te.onChangeInventory( this, slot, InvOperation.setInventorySlotContents, removed, added );
			this.te.saveChanges();
		}
	}

	@Override
	public String getInventoryName()
	{
		return "appeng-internal";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return this.maxStack > 64 ? 64 : this.maxStack;
	}

	@Override
	public void markDirty()
	{
		if( this.te != null && Platform.isServer() )
		{
			this.te.onChangeInventory( this, -1, InvOperation.markDirty, null, null );
			this.te.saveChanges();
		}
	}

	@Override
	public boolean isUseableByPlayer( final EntityPlayer var1 )
	{
		return true;
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		return true;
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
}
