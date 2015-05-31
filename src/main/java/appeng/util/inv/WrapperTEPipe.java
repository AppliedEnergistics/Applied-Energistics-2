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
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;


public final class WrapperTEPipe implements IInventory
{

	final TileEntity ad;
	final ForgeDirection dir;

	public WrapperTEPipe( TileEntity te, ForgeDirection d )
	{
		this.ad = te;
		this.dir = d;
	}

	@Override
	public final int getSizeInventory()
	{
		return 1;
	}

	@Override
	public final ItemStack getStackInSlot( int i )
	{
		return null;
	}

	@Override
	public final ItemStack decrStackSize( int i, int j )
	{
		return null;
	}

	@Override
	public final ItemStack getStackInSlotOnClosing( int i )
	{
		return null;
	}

	@Override
	public final void setInventorySlotContents( int i, ItemStack itemstack )
	{
		// ITE.addItemsToPipe( ad, itemstack, dir );
	}

	@Override
	public final String getInventoryName()
	{
		return null;
	}

	@Override
	public final boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public final int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public final void markDirty()
	{

	}

	@Override
	public final boolean isUseableByPlayer( EntityPlayer entityplayer )
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
