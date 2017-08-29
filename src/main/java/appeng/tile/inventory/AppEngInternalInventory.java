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


import appeng.api.storage.IMEInventory;
import appeng.core.AELog;
import appeng.me.storage.MEIInventoryWrapper;
import appeng.util.Platform;
import appeng.util.iterators.InvIterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Iterator;


public class AppEngInternalInventory implements IInventory, Iterable<ItemStack>
{

	private final int size;
	private final ItemStack[] inv;
	private boolean enableClientEvents = false;
	private IAEAppEngInventory te;
	private int maxStack;

	public AppEngInternalInventory( final IAEAppEngInventory inventory, final int size )
	{
		this.setTileEntity( inventory );
		this.size = size;
		this.maxStack = 64;
		this.inv = new ItemStack[size];
	}

	public IMEInventory getMEInventory()
	{
		return new MEIInventoryWrapper( this, null );
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

	@Override
	public int getSizeInventory()
	{
		return this.size;
	}

	@Override
	public ItemStack getStackInSlot( final int var1 )
	{
		return this.inv[var1];
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
				ns = this.inv[slot];
				this.inv[slot] = null;
			}
			else
			{
				ns = split.splitStack( qty );
			}

			if( this.getTileEntity() != null && this.eventsEnabled() )
			{
				this.getTileEntity().onChangeInventory( this, slot, InvOperation.decreaseStackSize, ns, null );
			}

			this.markDirty();
			return ns;
		}

		return null;
	}

	protected boolean eventsEnabled()
	{
		return Platform.isServer() || this.isEnableClientEvents();
	}

	@Override
	public ItemStack getStackInSlotOnClosing( final int var1 )
	{
		return null;
	}

	@Override
	public void setInventorySlotContents( final int slot, final ItemStack newItemStack )
	{
		final ItemStack oldStack = this.inv[slot];
		this.inv[slot] = newItemStack;

		if( this.getTileEntity() != null && this.eventsEnabled() )
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

			this.getTileEntity().onChangeInventory( this, slot, InvOperation.setInventorySlotContents, removed, added );

			this.markDirty();
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
		if( this.getTileEntity() != null && this.eventsEnabled() )
		{
			this.getTileEntity().onChangeInventory( this, -1, InvOperation.markDirty, null, null );
			this.getTileEntity().saveChanges();
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

	public void setMaxStackSize( final int s )
	{
		this.maxStack = s;
	}

	// for guis...
	public void markDirty( final int slotIndex )
	{
		if( this.getTileEntity() != null && this.eventsEnabled() )
		{
			this.getTileEntity().onChangeInventory( this, slotIndex, InvOperation.markDirty, null, null );
			this.getTileEntity().saveChanges();
		}
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

	public void readFromNBT( final NBTTagCompound target )
	{
		for( int x = 0; x < this.size; x++ )
		{
			try
			{
				final NBTTagCompound c = target.getCompoundTag( "#" + x );

				if( c != null )
				{
					this.inv[x] = ItemStack.loadItemStackFromNBT( c );
				}
			}
			catch( final Exception e )
			{
				AELog.debug( e );
			}
		}
	}

	@Override
	public Iterator<ItemStack> iterator()
	{
		return new InvIterator( this );
	}

	private boolean isEnableClientEvents()
	{
		return this.enableClientEvents;
	}

	public void setEnableClientEvents( final boolean enableClientEvents )
	{
		this.enableClientEvents = enableClientEvents;
	}

	private IAEAppEngInventory getTileEntity()
	{
		return this.te;
	}

	public void setTileEntity( final IAEAppEngInventory te )
	{
		this.te = te;
	}
}
