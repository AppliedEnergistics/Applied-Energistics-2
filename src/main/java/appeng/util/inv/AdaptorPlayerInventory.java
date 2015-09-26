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


public class AdaptorPlayerInventory implements IInventory
{

	private final IInventory src;
	private final int min = 0;
	private final int size = 36;

	public AdaptorPlayerInventory( final IInventory playerInv, final boolean swap )
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
	public int getSizeInventory()
	{
		return this.size;
	}

	@Override
	public ItemStack getStackInSlot( final int var1 )
	{
		return this.src.getStackInSlot( var1 + this.min );
	}

	@Override
	public ItemStack decrStackSize( final int var1, final int var2 )
	{
		return this.src.decrStackSize( this.min + var1, var2 );
	}

	@Override
	public ItemStack getStackInSlotOnClosing( final int var1 )
	{
		return this.src.getStackInSlotOnClosing( this.min + var1 );
	}

	@Override
	public void setInventorySlotContents( final int var1, final ItemStack var2 )
	{
		this.src.setInventorySlotContents( var1 + this.min, var2 );
	}

	@Override
	public String getInventoryName()
	{
		return this.src.getInventoryName();
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
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
	public void openInventory()
	{
		this.src.openInventory();
	}

	@Override
	public void closeInventory()
	{
		this.src.closeInventory();
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		return this.src.isItemValidForSlot( i, itemstack );
	}
}
