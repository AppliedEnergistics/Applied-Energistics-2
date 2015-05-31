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


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;


public class WrapperInventoryRange implements IInventory
{

	private final IInventory src;
	protected boolean ignoreValidItems = false;
	int[] slots;

	public WrapperInventoryRange( IInventory a, int[] s, boolean ignoreValid )
	{
		this.src = a;
		this.slots = s;

		if( this.slots == null )
		{
			this.slots = new int[0];
		}

		this.ignoreValidItems = ignoreValid;
	}

	public WrapperInventoryRange( IInventory a, int min, int size, boolean ignoreValid )
	{
		this.src = a;
		this.slots = new int[size];
		for( int x = 0; x < size; x++ )
		{
			this.slots[x] = min + x;
		}
		this.ignoreValidItems = ignoreValid;
	}

	public static String concatLines( int[] s, String separator )
	{
		if( s.length > 0 )
		{
			StringBuilder sb = new StringBuilder();
			for( int value : s )
			{
				if( sb.length() > 0 )
				{
					sb.append( separator );
				}
				sb.append( value );
			}
			return sb.toString();
		}
		return "";
	}

	@Override
	public final int getSizeInventory()
	{
		return this.slots.length;
	}

	@Override
	public final ItemStack getStackInSlot( int var1 )
	{
		return this.src.getStackInSlot( this.slots[var1] );
	}

	@Override
	public ItemStack decrStackSize( int var1, int var2 )
	{
		return this.src.decrStackSize( this.slots[var1], var2 );
	}

	@Override
	public final ItemStack getStackInSlotOnClosing( int var1 )
	{
		return this.src.getStackInSlotOnClosing( this.slots[var1] );
	}

	@Override
	public final void setInventorySlotContents( int var1, ItemStack var2 )
	{
		this.src.setInventorySlotContents( this.slots[var1], var2 );
	}

	@Override
	public final String getInventoryName()
	{
		return this.src.getInventoryName();
	}

	@Override
	public final boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public final int getInventoryStackLimit()
	{
		return this.src.getInventoryStackLimit();
	}

	@Override
	public final void markDirty()
	{
		this.src.markDirty();
	}

	@Override
	public final boolean isUseableByPlayer( EntityPlayer var1 )
	{
		return this.src.isUseableByPlayer( var1 );
	}

	@Override
	public final void openInventory()
	{
		this.src.openInventory();
	}

	@Override
	public final void closeInventory()
	{
		this.src.closeInventory();
	}

	@Override
	public boolean isItemValidForSlot( int i, ItemStack itemstack )
	{
		if( this.ignoreValidItems )
		{
			return true;
		}

		return this.src.isItemValidForSlot( this.slots[i], itemstack );
	}
}
