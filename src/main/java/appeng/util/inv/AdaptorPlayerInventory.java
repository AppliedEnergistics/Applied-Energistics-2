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


public final class AdaptorPlayerInventory implements IInventory
{

	private final IInventory src;
	private final int min = 0;
	private final int size = 36;

	public AdaptorPlayerInventory( IInventory playerInv, boolean swap )
	{

		if( swap )
		{
			this.src = new WrapperChainedInventory( new WrapperInventoryRange( playerInv, 9, this.size - 9, false ), new WrapperInventoryRange( playerInv, 0, 9, false ) );
		}
		else
		{
			this.src = playerInv;
		}
	}

	@Override
	public final int getSizeInventory()
	{
		return this.size;
	}

	@Override
	public final ItemStack getStackInSlot( int var1 )
	{
		return this.src.getStackInSlot( var1 + this.min );
	}

	@Override
	public final ItemStack decrStackSize( int var1, int var2 )
	{
		return this.src.decrStackSize( this.min + var1, var2 );
	}

	@Override
	public final ItemStack getStackInSlotOnClosing( int var1 )
	{
		return this.src.getStackInSlotOnClosing( this.min + var1 );
	}

	@Override
	public final void setInventorySlotContents( int var1, ItemStack var2 )
	{
		this.src.setInventorySlotContents( var1 + this.min, var2 );
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
	public final boolean isItemValidForSlot( int i, ItemStack itemstack )
	{
		return this.src.isItemValidForSlot( i, itemstack );
	}
}
