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
import net.minecraft.util.IChatComponent;


public class AppEngNullInventory implements IInventory
{

	public AppEngNullInventory()
	{
	}

	public void writeToNBT( NBTTagCompound target )
	{
	}

	@Override
	public int getSizeInventory()
	{
		return 0;
	}

	@Override
	public ItemStack getStackInSlot( int var1 )
	{
		return null;
	}

	@Override
	public ItemStack decrStackSize( int slot, int qty )
	{
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing( int var1 )
	{
		return null;
	}

	@Override
	public void setInventorySlotContents( int slot, ItemStack newItemStack )
	{

	}

	@Override
	public String getName()
	{
		return "appeng-internal";
	}

	@Override
	public boolean hasCustomName()
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
	public boolean isUseableByPlayer( EntityPlayer var1 )
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot( int i, ItemStack itemstack )
	{
		return false;
	}

	@Override
	public IChatComponent getDisplayName()
	{
		return null;
	}

	@Override
	public void openInventory(
			EntityPlayer player )
	{
				
	}

	@Override
	public void closeInventory(
			EntityPlayer player )
	{
		
	}

	@Override
	public int getField(
			int id )
	{
		return 0;
	}

	@Override
	public void setField(
			int id,
			int value )
	{
		
	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		
	}
}
