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
import net.minecraft.util.EnumFacing;


public class WrapperMCISidedInventory extends WrapperInventoryRange implements IInventoryWrapper
{

	final ISidedInventory side;
	private final EnumFacing dir;

	public WrapperMCISidedInventory( ISidedInventory a, EnumFacing d )
	{
		super( a, a.getSlotsForFace( d ), false );
		this.side = a;
		this.dir = d;
	}

	@Override
	public ItemStack decrStackSize( int var1, int var2 )
	{
		if( this.canRemoveItemFromSlot( var1, this.getStackInSlot( var1 ) ) )
		{
			return super.decrStackSize( var1, var2 );
		}
		return null;
	}

	@Override
	public boolean isItemValidForSlot( int i, ItemStack itemstack )
	{

		if( this.ignoreValidItems )
		{
			return true;
		}

		if( this.side.isItemValidForSlot( this.slots[i], itemstack ) )
		{
			return this.side.canInsertItem( this.slots[i], itemstack, this.dir );
		}

		return false;
	}

	@Override
	public boolean canRemoveItemFromSlot( int i, ItemStack is )
	{
		if( is == null )
		{
			return false;
		}

		return this.side.canExtractItem( this.slots[i], is, this.dir );
	}
}
