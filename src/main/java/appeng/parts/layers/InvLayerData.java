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

package appeng.parts.layers;


import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;


public class InvLayerData
{

	// a simple empty array for empty stuff..
	private static final int[] NULL_SIDES = {};

	// cache of inventory state.
	private final int[][] sides;
	private final List<ISidedInventory> inventories;
	private final List<InvSot> slots;

	public InvLayerData( final int[][] a, final List<ISidedInventory> b, final List<InvSot> c )
	{
		this.sides = a;
		this.inventories = b;
		this.slots = c;
	}

	public ItemStack decreaseStackSize( final int slot, final int amount )
	{
		if( this.isSlotValid( slot ) )
		{
			return this.slots.get( slot ).decreaseStackSize( amount );
		}

		return null;
	}

	/**
	 * check if a slot index is valid, prevent crashes from bad code :)
	 *
	 * @param slot slot index
	 *
	 * @return true, if the slot exists.
	 */
	boolean isSlotValid( final int slot )
	{
		return this.slots != null && slot >= 0 && slot < this.slots.size();
	}

	public int getSizeInventory()
	{
		if( this.slots == null )
		{
			return 0;
		}

		return this.slots.size();
	}

	public ItemStack getStackInSlot( final int slot )
	{
		if( this.isSlotValid( slot ) )
		{
			return this.slots.get( slot ).getStackInSlot();
		}

		return null;
	}

	public boolean isItemValidForSlot( final int slot, final ItemStack itemstack )
	{
		if( this.isSlotValid( slot ) )
		{
			return this.slots.get( slot ).isItemValidForSlot( itemstack );
		}

		return false;
	}

	public void setInventorySlotContents( final int slot, final ItemStack itemstack )
	{
		if( this.isSlotValid( slot ) )
		{
			this.slots.get( slot ).setInventorySlotContents( itemstack );
		}
	}

	public boolean canExtractItem( final int slot, final ItemStack itemstack, final int side )
	{
		if( this.isSlotValid( slot ) )
		{
			return this.slots.get( slot ).canExtractItem( itemstack, side );
		}

		return false;
	}

	public boolean canInsertItem( final int slot, final ItemStack itemstack, final int side )
	{
		if( this.isSlotValid( slot ) )
		{
			return this.slots.get( slot ).canInsertItem( itemstack, side );
		}

		return false;
	}

	public void markDirty()
	{
		if( this.inventories != null )
		{
			for( final IInventory inv : this.inventories )
			{
				inv.markDirty();
			}
		}
	}

	public int[] getAccessibleSlotsFromSide( final int side )
	{
		if( this.sides == null || side < 0 || side > 5 )
		{
			return NULL_SIDES;
		}
		return this.sides[side];
	}
}
