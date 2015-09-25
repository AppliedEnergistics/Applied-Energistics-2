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

package appeng.tile.inventory;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;


public class AppEngNullInventory implements IInventory
{

	public AppEngNullInventory()
	{
	}

	public void writeToNBT( final NBTTagCompound target )
	{
	}

	@Override
	public int getSizeInventory()
	{
		return 0;
	}

	@Override
	public ItemStack getStackInSlot( final int var1 )
	{
		return null;
	}

	@Override
	public ItemStack decrStackSize( final int slot, final int qty )
	{
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing( final int var1 )
	{
		return null;
	}

	@Override
	public void setInventorySlotContents( final int slot, final ItemStack newItemStack )
	{

	}

	@Override
	public String getInventoryName()
	{
		return "appeng-internal";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 0;
	}

	@Override
	public void markDirty()
	{

	}

	@Override
	public boolean isUseableByPlayer( final EntityPlayer var1 )
	{
		return false;
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		return false;
	}
}
