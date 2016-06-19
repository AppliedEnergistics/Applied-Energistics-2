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

package appeng.util.inv;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;


public class WrapperInventoryRange implements IInventory
{

	private final IInventory src;
	private boolean ignoreValidItems = false;
	private int[] slots;

	public WrapperInventoryRange( final IInventory a, final int[] s, final boolean ignoreValid )
	{
		this.src = a;
		this.setSlots( s );

		if( this.getSlots() == null )
		{
			this.setSlots( new int[0] );
		}

		this.setIgnoreValidItems( ignoreValid );
	}

	public WrapperInventoryRange( final IInventory a, final int min, final int size, final boolean ignoreValid )
	{
		this.src = a;
		this.setSlots( new int[size] );
		for( int x = 0; x < size; x++ )
		{
			this.getSlots()[x] = min + x;
		}
		this.setIgnoreValidItems( ignoreValid );
	}

	public static String concatLines( final int[] s, final String separator )
	{
		if( s.length > 0 )
		{
			final StringBuilder sb = new StringBuilder();
			for( final int value : s )
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
	public int getSizeInventory()
	{
		return this.getSlots().length;
	}

	@Override
	public ItemStack getStackInSlot( final int var1 )
	{
		return this.src.getStackInSlot( this.getSlots()[var1] );
	}

	@Override
	public ItemStack decrStackSize( final int var1, final int var2 )
	{
		return this.src.decrStackSize( this.getSlots()[var1], var2 );
	}

	@Override
	public ItemStack removeStackFromSlot( final int var1 )
	{
		return this.src.removeStackFromSlot( this.getSlots()[var1] );
	}

	@Override
	public void setInventorySlotContents( final int var1, final ItemStack var2 )
	{
		this.src.setInventorySlotContents( this.getSlots()[var1], var2 );
	}

	@Override
	public int getInventoryStackLimit()
	{
		return this.src.getInventoryStackLimit();
	}

	@Override
	public void markDirty()
	{
		this.src.markDirty();
	}

	@Override
	public boolean isUseableByPlayer( final EntityPlayer var1 )
	{
		return this.src.isUseableByPlayer( var1 );
	}

	@Override
	public String getName()
	{
		return this.src.getName();
	}

	@Override
	public boolean hasCustomName()
	{
		return this.src.hasCustomName();
	}

	@Override
	public void openInventory( final EntityPlayer player )
	{
		this.src.openInventory( player );
	}

	@Override
	public void closeInventory( final EntityPlayer player )
	{
		this.src.closeInventory( player );
	}

	@Override
	public void clear()
	{
		this.src.clear();
	}

	@Override
	public int getField( final int id )
	{
		return this.src.getField( id );
	}

	@Override
	public int getFieldCount()
	{
		return this.src.getFieldCount();
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return this.src.getDisplayName();
	}

	@Override
	public void setField( final int id, final int value )
	{
		this.src.setField( id, value );
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		if( this.isIgnoreValidItems() )
		{
			return true;
		}

		return this.src.isItemValidForSlot( this.getSlots()[i], itemstack );
	}

	boolean isIgnoreValidItems()
	{
		return this.ignoreValidItems;
	}

	private void setIgnoreValidItems( final boolean ignoreValidItems )
	{
		this.ignoreValidItems = ignoreValidItems;
	}

	int[] getSlots()
	{
		return this.slots;
	}

	private void setSlots( final int[] slots )
	{
		this.slots = slots;
	}
}
