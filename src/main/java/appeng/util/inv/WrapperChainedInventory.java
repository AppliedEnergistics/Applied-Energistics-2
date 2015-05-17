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

package appeng.util.inv;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;


public final class WrapperChainedInventory implements IInventory
{

	int fullSize = 0;
	private List<IInventory> l;
	private Map<Integer, InvOffset> offsets;

	public WrapperChainedInventory( IInventory... inventories )
	{
		this.setInventory( inventories );
	}

	public final void setInventory( IInventory... a )
	{
		this.l = ImmutableList.copyOf( a );
		this.calculateSizes();
	}

	public final void calculateSizes()
	{
		this.offsets = new HashMap<Integer, WrapperChainedInventory.InvOffset>();

		int offset = 0;
		for( IInventory in : this.l )
		{
			InvOffset io = new InvOffset();
			io.offset = offset;
			io.size = in.getSizeInventory();
			io.i = in;

			for( int y = 0; y < io.size; y++ )
			{
				this.offsets.put( y + io.offset, io );
			}

			offset += io.size;
		}

		this.fullSize = offset;
	}

	public WrapperChainedInventory( List<IInventory> inventories )
	{
		this.setInventory( inventories );
	}

	public final void setInventory( List<IInventory> a )
	{
		this.l = a;
		this.calculateSizes();
	}

	public final void cycleOrder()
	{
		if( this.l.size() > 1 )
		{
			List<IInventory> newOrder = new ArrayList<IInventory>( this.l.size() );
			newOrder.add( this.l.get( this.l.size() - 1 ) );
			for( int x = 0; x < this.l.size() - 1; x++ )
			{
				newOrder.add( this.l.get( x ) );
			}
			this.setInventory( newOrder );
		}
	}

	public IInventory getInv( int idx )
	{
		InvOffset io = this.offsets.get( idx );
		if( io != null )
		{
			return io.i;
		}
		return null;
	}

	public int getInvSlot( int idx )
	{
		InvOffset io = this.offsets.get( idx );
		if( io != null )
		{
			return idx - io.offset;
		}
		return 0;
	}

	@Override
	public final int getSizeInventory()
	{
		return this.fullSize;
	}

	@Override
	public final ItemStack getStackInSlot( int idx )
	{
		InvOffset io = this.offsets.get( idx );
		if( io != null )
		{
			return io.i.getStackInSlot( idx - io.offset );
		}
		return null;
	}

	@Override
	public final ItemStack decrStackSize( int idx, int var2 )
	{
		InvOffset io = this.offsets.get( idx );
		if( io != null )
		{
			return io.i.decrStackSize( idx - io.offset, var2 );
		}
		return null;
	}

	@Override
	public final ItemStack getStackInSlotOnClosing( int idx )
	{
		InvOffset io = this.offsets.get( idx );
		if( io != null )
		{
			return io.i.getStackInSlotOnClosing( idx - io.offset );
		}
		return null;
	}

	@Override
	public final void setInventorySlotContents( int idx, ItemStack var2 )
	{
		InvOffset io = this.offsets.get( idx );
		if( io != null )
		{
			io.i.setInventorySlotContents( idx - io.offset, var2 );
		}
	}

	@Override
	public final String getInventoryName()
	{
		return "ChainedInv";
	}

	@Override
	public final boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public final int getInventoryStackLimit()
	{
		int smallest = 64;

		for( IInventory i : this.l )
		{
			smallest = Math.min( smallest, i.getInventoryStackLimit() );
		}

		return smallest;
	}

	@Override
	public final void markDirty()
	{
		for( IInventory i : this.l )
		{
			i.markDirty();
		}
	}

	@Override
	public final boolean isUseableByPlayer( EntityPlayer var1 )
	{
		return false;
	}

	@Override
	public final void openInventory()
	{
	}

	@Override
	public final void closeInventory()
	{
	}

	@Override
	public final boolean isItemValidForSlot( int idx, ItemStack itemstack )
	{
		InvOffset io = this.offsets.get( idx );
		if( io != null )
		{
			return io.i.isItemValidForSlot( idx - io.offset, itemstack );
		}
		return false;
	}

	static final class InvOffset
	{

		int offset;
		int size;
		IInventory i;
	}
}
