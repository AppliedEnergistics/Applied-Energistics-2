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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;

import appeng.core.AELog;
import appeng.util.Platform;
import appeng.util.iterators.InvIterator;


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

	@Override
	public boolean isEmpty()
	{
		for( int x = 0; x < this.size; x++ )
		{
			if( !this.getStackInSlot( x ).isEmpty() )
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
		return this.inv[var1] == null ? ItemStack.EMPTY : this.inv[var1];
	}

	@Override
	public ItemStack decrStackSize( final int slot, final int qty )
	{
		if( this.inv[slot] != null )
		{
			final ItemStack split = this.getStackInSlot( slot );
			ItemStack ns = ItemStack.EMPTY;

			if( qty >= split.getCount() )
			{
				ns = this.inv[slot];
				this.inv[slot] = ItemStack.EMPTY;
			}
			else
			{
				ns = split.splitStack( qty );
			}

			if( this.getTileEntity() != null && this.eventsEnabled() )
			{
				this.getTileEntity().onChangeInventory( this, slot, InvOperation.decreaseStackSize, ns, ItemStack.EMPTY );
			}

			this.markDirty();
			return ns;
		}

		return ItemStack.EMPTY;
	}

	protected boolean eventsEnabled()
	{
		return Platform.isServer() || this.isEnableClientEvents();
	}

	@Override
	public ItemStack removeStackFromSlot( final int var1 )
	{
		return ItemStack.EMPTY;
	}

	private ItemStack getOldStack( int slot )
	{
		if( this.inv[slot] == null )
		{
			return ItemStack.EMPTY;
		}

		return this.inv[slot];
	}

	@Override
	public void setInventorySlotContents( final int slot, final ItemStack newItemStack )
	{
		final ItemStack oldStack = this.getOldStack( slot );
		this.inv[slot] = newItemStack;

		if( this.getTileEntity() != null && this.eventsEnabled() )
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

			this.getTileEntity().onChangeInventory( this, slot, InvOperation.setInventorySlotContents, removed, added );

			this.markDirty();
		}
	}

	@Override
	public String getName()
	{
		return "appeng-internal";
	}

	@Override
	public boolean hasCustomName()
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
			this.getTileEntity().onChangeInventory( this, -1, InvOperation.markDirty, ItemStack.EMPTY, ItemStack.EMPTY );
		}
	}

	@Override
	public boolean isUsableByPlayer( final EntityPlayer var1 )
	{
		return true;
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
			this.getTileEntity().onChangeInventory( this, slotIndex, InvOperation.markDirty, ItemStack.EMPTY, ItemStack.EMPTY );
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
					this.inv[x] = new ItemStack( c );
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

	@Override
	public ITextComponent getDisplayName()
	{
		return null;
	}

	@Override
	public void openInventory( final EntityPlayer player )
	{

	}

	@Override
	public void closeInventory( final EntityPlayer player )
	{

	}

	@Override
	public int getField( final int id )
	{
		return 0;
	}

	@Override
	public void setField( final int id, final int value )
	{
	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		for( int x = 0; x < this.size; x++ )
		{
			this.setInventorySlotContents( x, ItemStack.EMPTY );
		}
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
