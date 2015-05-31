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

package appeng.tile.inventory;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;


public final class AppEngNullInventory implements IInventory
{

	public AppEngNullInventory()
	{
	}

	public void writeToNBT( NBTTagCompound target )
	{
	}

	@Override
	public final int getSizeInventory()
	{
		return 0;
	}

	@Override
	public final ItemStack getStackInSlot( int var1 )
	{
		return null;
	}

	@Override
	public final ItemStack decrStackSize( int slot, int qty )
	{
		return null;
	}

	@Override
	public final ItemStack getStackInSlotOnClosing( int var1 )
	{
		return null;
	}

	@Override
	public final void setInventorySlotContents( int slot, ItemStack newItemStack )
	{

	}

	@Override
	public final String getInventoryName()
	{
		return "appeng-internal";
	}

	@Override
	public final boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public final int getInventoryStackLimit()
	{
		return 0;
	}

	@Override
	public final void markDirty()
	{

	}

	@Override
	public final boolean isUseableByPlayer( EntityPlayer var1 )
	{
		return false;
	}

	@Override
	public final void openInventory()
	{
	}

	@Override
	public final void closeInventory()
	{
	}

	@Override
	public final boolean isItemValidForSlot( int i, ItemStack itemstack )
	{
		return false;
	}
}
