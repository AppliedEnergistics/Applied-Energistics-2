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

package appeng.container.slot;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;


public class NullSlot extends Slot
{

	public NullSlot()
	{
		super( null, 0, 0, 0 );
	}

	@Override
	public void onSlotChange( ItemStack par1ItemStack, ItemStack par2ItemStack )
	{

	}

	@Override
	public void onPickupFromSlot( EntityPlayer par1EntityPlayer, ItemStack par2ItemStack )
	{

	}

	@Override
	public boolean isItemValid( ItemStack par1ItemStack )
	{
		return false;
	}

	@Override
	public ItemStack getStack()
	{
		return null;
	}

	@Override
	public void putStack( ItemStack par1ItemStack )
	{

	}

	@Override
	public void onSlotChanged()
	{

	}

	@Override
	public int getSlotStackLimit()
	{
		return 0;
	}

	@Override
	public ItemStack decrStackSize( int par1 )
	{
		return null;
	}

	@Override
	public boolean isSlotInInventory( IInventory par1IInventory, int par2 )
	{
		return false;
	}

	@Override
	public boolean canTakeStack( EntityPlayer par1EntityPlayer )
	{
		return false;
	}

	@Override
	public int getSlotIndex()
	{
		return 0;
	}
}
