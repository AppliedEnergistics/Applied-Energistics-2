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


import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;


public class InvSot
{

	private final ISidedInventory partInv;
	private final int index;

	public InvSot( final ISidedInventory part, final int slot )
	{
		this.partInv = part;
		this.index = slot;
	}

	ItemStack decreaseStackSize( final int j )
	{
		return this.partInv.decrStackSize( this.index, j );
	}

	ItemStack getStackInSlot()
	{
		return this.partInv.getStackInSlot( this.index );
	}

	boolean isItemValidForSlot( final ItemStack itemstack )
	{
		return this.partInv.isItemValidForSlot( this.index, itemstack );
	}

	void setInventorySlotContents( final ItemStack itemstack )
	{
		this.partInv.setInventorySlotContents( this.index, itemstack );
	}

	boolean canExtractItem( final ItemStack itemstack, final int side )
	{
		return this.partInv.canExtractItem( this.index, itemstack, side );
	}

	boolean canInsertItem( final ItemStack itemstack, final int side )
	{
		return this.partInv.canInsertItem( this.index, itemstack, side );
	}
}
