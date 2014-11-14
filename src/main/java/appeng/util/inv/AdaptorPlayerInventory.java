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
	private final int min=0;
	private final int size=36;

	public AdaptorPlayerInventory(IInventory playerInv, boolean swap)
	{
		
		if ( swap )
			src = new WrapperChainedInventory( new WrapperInventoryRange( playerInv, 9, size-9, false ), new WrapperInventoryRange( playerInv, 0, 9, false )  );
		else
			src = playerInv;
		
	}

	@Override
	public int getSizeInventory()
	{
		return size;
	}

	@Override
	public ItemStack getStackInSlot(int var1)
	{
		return src.getStackInSlot( var1 + min );
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2)
	{
		return src.decrStackSize( min + var1, var2 );
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1)
	{
		return src.getStackInSlotOnClosing( min + var1 );
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2)
	{
		src.setInventorySlotContents( var1 + min, var2 );
	}

	@Override
	public String getInventoryName()
	{
		return src.getInventoryName();
	}

	@Override
	public int getInventoryStackLimit()
	{
		return src.getInventoryStackLimit();
	}

	@Override
	public void markDirty()
	{
		src.markDirty();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1)
	{
		return src.isUseableByPlayer( var1 );
	}

	@Override
	public void openInventory()
	{
		src.openInventory();
	}

	@Override
	public void closeInventory()
	{
		src.closeInventory();
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}

}
