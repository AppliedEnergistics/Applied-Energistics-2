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

import appeng.api.storage.IMEInventory;
import appeng.core.AELog;
import appeng.me.storage.MEIInventoryWrapper;
import appeng.util.Platform;
import appeng.util.iterators.InvIterator;

public class AppEngInternalInventory implements IInventory, Iterable<ItemStack>
{

	protected IAEAppEngInventory te;
	protected final int size;
	protected int maxStack;

	public boolean enableClientEvents = false;
	protected final ItemStack[] inv;

	public IMEInventory getMEInventory()
	{
		return new MEIInventoryWrapper( this, null );
	}

	public boolean isEmpty()
	{
		for (int x = 0; x < this.getSizeInventory(); x++)
			if ( this.getStackInSlot( x ) != null )
				return false;
		return true;
	}

	public AppEngInternalInventory(IAEAppEngInventory _te, int s) {
		this.te = _te;
		this.size = s;
		this.maxStack = 64;
		this.inv = new ItemStack[s];
	}

	protected boolean eventsEnabled()
	{
		return Platform.isServer() || this.enableClientEvents;
	}

	public void setMaxStackSize(int s)
	{
		this.maxStack = s;
	}

	@Override
	public ItemStack getStackInSlot(int var1)
	{
		return this.inv[var1];
	}

	@Override
	public ItemStack decrStackSize(int slot, int qty)
	{
		if ( this.inv[slot] != null )
		{
			ItemStack split = this.getStackInSlot( slot );
			ItemStack ns = null;

			if ( qty >= split.stackSize )
			{
				ns = this.inv[slot];
				this.inv[slot] = null;
			}
			else
				ns = split.splitStack( qty );

			if ( this.te != null && this.eventsEnabled() )
			{
				this.te.onChangeInventory( this, slot, InvOperation.decreaseStackSize, ns, null );
			}

			this.markDirty();
			return ns;
		}

		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1)
	{
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack newItemStack)
	{
		ItemStack oldStack = this.inv[slot];
		this.inv[slot] = newItemStack;

		if ( this.te != null && this.eventsEnabled() )
		{
			ItemStack removed = oldStack;
			ItemStack added = newItemStack;

			if ( oldStack != null && newItemStack != null && Platform.isSameItem( oldStack, newItemStack ) )
			{
				if ( oldStack.stackSize > newItemStack.stackSize )
				{
					removed = removed.copy();
					removed.stackSize -= newItemStack.stackSize;
					added = null;
				}
				else if ( oldStack.stackSize < newItemStack.stackSize )
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

			this.markDirty();
		}
	}

	@Override
	public void markDirty()
	{
		if ( this.te != null && this.eventsEnabled() )
		{
			this.te.onChangeInventory( this, -1, InvOperation.markDirty, null, null );
		}
	}

	// for guis...
	public void markDirty(int slotIndex)
	{
		if ( this.te != null && this.eventsEnabled() )
		{
			this.te.onChangeInventory( this, slotIndex, InvOperation.markDirty, null, null );
		}
	}

	@Override
	public int getInventoryStackLimit()
	{
		return this.maxStack > 64 ? 64 : this.maxStack;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1)
	{
		return true;
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public void openInventory()
	{
	}

	public void writeToNBT(NBTTagCompound target)
	{
		for (int x = 0; x < this.size; x++)
		{
			try
			{
				NBTTagCompound c = new NBTTagCompound();

				if ( this.inv[x] != null )
				{
					this.inv[x].writeToNBT( c );
				}

				target.setTag( "#" + x, c );
			}
			catch (Exception ignored)
			{
			}
		}
	}

	public void readFromNBT(NBTTagCompound target)
	{
		for (int x = 0; x < this.size; x++)
		{
			try
			{
				NBTTagCompound c = target.getCompoundTag( "#" + x );

				if ( c != null )
					this.inv[x] = ItemStack.loadItemStackFromNBT( c );

			}
			catch (Exception e)
			{
				AELog.error( e );
			}
		}
	}

	public void writeToNBT(NBTTagCompound data, String name)
	{
		NBTTagCompound c = new NBTTagCompound();
		this.writeToNBT( c );
		data.setTag( name, c );
	}

	public void readFromNBT(NBTTagCompound data, String name)
	{
		NBTTagCompound c = data.getCompoundTag( name );
		if ( c != null )
			this.readFromNBT( c );
	}

	@Override
	public int getSizeInventory()
	{
		return this.size;
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
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}

	@Override
	public Iterator<ItemStack> iterator()
	{
		return new InvIterator( this );
	}

}
