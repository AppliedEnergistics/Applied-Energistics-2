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

	final public ISidedInventory partInv;
	final public int index;

	public InvSot(ISidedInventory part, int slot) {
		partInv = part;
		index = slot;
	}

	public ItemStack decreaseStackSize(int j)
	{
		return partInv.decrStackSize( index, j );
	}

	public ItemStack getStackInSlot()
	{
		return partInv.getStackInSlot( index );
	}

	public boolean isItemValidForSlot(ItemStack itemstack)
	{
		return partInv.isItemValidForSlot( index, itemstack );
	}

	public void setInventorySlotContents(ItemStack itemstack)
	{
		partInv.setInventorySlotContents( index, itemstack );
	}

	public boolean canExtractItem(ItemStack itemstack, int side)
	{
		return partInv.canExtractItem( index, itemstack, side );
	}

	public boolean canInsertItem(ItemStack itemstack, int side)
	{
		return partInv.canInsertItem( index, itemstack, side );
	}

}
