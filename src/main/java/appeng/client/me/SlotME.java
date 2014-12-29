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

package appeng.client.me;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import appeng.api.storage.data.IAEItemStack;

public class SlotME extends Slot
{

	public final InternalSlotME mySlot;

	public SlotME(InternalSlotME me) {
		super( null, 0, me.xPos, me.yPos );
		this.mySlot = me;
	}

	@Override
	public ItemStack getStack()
	{
		if ( this.mySlot.hasPower() )
			return this.mySlot.getStack();
		return null;
	}

	public IAEItemStack getAEStack()
	{
		if ( this.mySlot.hasPower() )
			return this.mySlot.getAEStack();
		return null;
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
	{
		return false;
	}

	@Override
	public ItemStack decrStackSize(int par1)
	{
		return null;
	}

	@Override
	public void putStack(ItemStack par1ItemStack)
	{

	}

	@Override
	public boolean getHasStack()
	{
		if ( this.mySlot.hasPower() )
			return this.getStack() != null;
		return false;
	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack)
	{
		return false;
	}

	@Override
	public int getSlotStackLimit()
	{
		return 0;
	}

	@Override
	public boolean isSlotInInventory(IInventory par1iInventory, int par2)
	{
		return false;
	}

	@Override
	public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack)
	{
	}

}
