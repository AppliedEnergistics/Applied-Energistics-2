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

package appeng.parts.layers;


import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;


public final class InvSot
{

	public final ISidedInventory partInv;
	public final int index;

	public InvSot( ISidedInventory part, int slot )
	{
		this.partInv = part;
		this.index = slot;
	}

	public final ItemStack decreaseStackSize( int j )
	{
		return this.partInv.decrStackSize( this.index, j );
	}

	public final ItemStack getStackInSlot()
	{
		return this.partInv.getStackInSlot( this.index );
	}

	public final boolean isItemValidForSlot( ItemStack itemstack )
	{
		return this.partInv.isItemValidForSlot( this.index, itemstack );
	}

	public final void setInventorySlotContents( ItemStack itemstack )
	{
		this.partInv.setInventorySlotContents( this.index, itemstack );
	}

	public final boolean canExtractItem( ItemStack itemstack, int side )
	{
		return this.partInv.canExtractItem( this.index, itemstack, side );
	}

	public final boolean canInsertItem( ItemStack itemstack, int side )
	{
		return this.partInv.canInsertItem( this.index, itemstack, side );
	}
}
