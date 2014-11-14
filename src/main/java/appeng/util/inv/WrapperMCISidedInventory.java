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

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public class WrapperMCISidedInventory extends WrapperInventoryRange implements IInventoryWrapper
{

	private final ForgeDirection dir;
	final ISidedInventory side;

	public WrapperMCISidedInventory(ISidedInventory a, ForgeDirection d) {
		super( a, a.getAccessibleSlotsFromSide( d.ordinal() ), false );
		side = a;
		dir = d;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{

		if ( ignoreValidItems )
			return true;

		if ( side.isItemValidForSlot( slots[i], itemstack ) )
			return side.canInsertItem( slots[i], itemstack, dir.ordinal() );

		return false;
	}

	@Override
	public boolean canRemoveItemFromSlot(int i, ItemStack is)
	{
		if ( is == null )
			return false;

		return side.canExtractItem( slots[i], is, dir.ordinal() );
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2)
	{
		if ( canRemoveItemFromSlot( var1, getStackInSlot( var1 ) ) )
			return super.decrStackSize( var1, var2 );
		return null;
	}

}
