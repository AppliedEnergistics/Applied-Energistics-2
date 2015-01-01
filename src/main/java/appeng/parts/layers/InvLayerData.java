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
	private final static int[] nullSides = new int[] {};

	// cache of inventory state.
	final private int[][] sides;
	final private List<ISidedInventory> inventories;
	final private List<InvSot> slots;

	public InvLayerData( int[][] a, List<ISidedInventory> b, List<InvSot> c) {
		this.sides = a;
		this.inventories = b;
		this.slots = c;
	}

	/**
	 * check if a slot index is valid, prevent crashes from bad code :)
	 * 
	 * @param slot slot index
	 * @return true, if the slot exists.
	 */
	boolean isSlotValid(int slot)
	{
		return this.slots != null && slot >= 0 && slot < this.slots.size();
	}

	public ItemStack decreaseStackSize(int slot, int amount)
	{
		if ( this.isSlotValid( slot ) )
			return this.slots.get( slot ).decreaseStackSize( amount );

		return null;
	}

	public int getSizeInventory()
	{
		if ( this.slots == null )
			return 0;

		return this.slots.size();
	}

	public ItemStack getStackInSlot(int slot)
	{
		if ( this.isSlotValid( slot ) )
			return this.slots.get( slot ).getStackInSlot();

		return null;
	}

	public boolean isItemValidForSlot(int slot, ItemStack itemstack)
	{
		if ( this.isSlotValid( slot ) )
			return this.slots.get( slot ).isItemValidForSlot( itemstack );

		return false;
	}

	public void setInventorySlotContents(int slot, ItemStack itemstack)
	{
		if ( this.isSlotValid( slot ) )
			this.slots.get( slot ).setInventorySlotContents( itemstack );
	}

	public boolean canExtractItem(int slot, ItemStack itemstack, int side)
	{
		if ( this.isSlotValid( slot ) )
			return this.slots.get( slot ).canExtractItem( itemstack, side );

		return false;
	}

	public boolean canInsertItem(int slot, ItemStack itemstack, int side)
	{
		if ( this.isSlotValid( slot ) )
			return this.slots.get( slot ).canInsertItem( itemstack, side );

		return false;
	}

	public void markDirty()
	{
		if ( this.inventories != null )
		{
			for (IInventory inv : this.inventories)
				inv.markDirty();
		}
	}

	public int[] getAccessibleSlotsFromSide(int side)
	{
		if ( this.sides == null || side < 0 || side > 5 )
			return nullSides;
		return this.sides[side];
	}

}
