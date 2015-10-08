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

	private final ISidedInventory side;
	private final ForgeDirection dir;

	public WrapperMCISidedInventory( final ISidedInventory a, final ForgeDirection d )
	{
		super( a, a.getAccessibleSlotsFromSide( d.ordinal() ), false );
		this.side = a;
		this.dir = d;
	}

	@Override
	public ItemStack decrStackSize( final int var1, final int var2 )
	{
		if( this.canRemoveItemFromSlot( var1, this.getStackInSlot( var1 ) ) )
		{
			return super.decrStackSize( var1, var2 );
		}
		return null;
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{

		if( this.isIgnoreValidItems() )
		{
			return true;
		}

		if( this.side.isItemValidForSlot( this.getSlots()[i], itemstack ) )
		{
			return this.side.canInsertItem( this.getSlots()[i], itemstack, this.dir.ordinal() );
		}

		return false;
	}

	@Override
	public boolean canRemoveItemFromSlot( final int i, final ItemStack is )
	{
		if( is == null )
		{
			return false;
		}

		return this.side.canExtractItem( this.getSlots()[i], is, this.dir.ordinal() );
	}
}
