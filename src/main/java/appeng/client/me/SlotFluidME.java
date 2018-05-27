/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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


import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;

import appeng.api.storage.data.IAEFluidStack;
import appeng.container.slot.ISlotFluid;


/**
 * @author BrockWS
 * @version rv6 - 22/05/2018
 * @since rv6 22/05/2018
 */
public class SlotFluidME extends SlotItemHandler implements ISlotFluid
{
	private InternalFluidSlotME slot;

	public SlotFluidME( InternalFluidSlotME slot )
	{
		super( null, 0, slot.getxPosition(), slot.getyPosition() );
		this.slot = slot;
	}

	public IAEFluidStack getAEStack()
	{
		if( this.slot.hasPower() )
		{
			return this.slot.getAEStack();
		}
		return null;
	}

	@Override
	public boolean isItemValid( final ItemStack par1ItemStack )
	{
		return false;
	}

	@Nonnull
	@Override
	public ItemStack getStack()
	{
		return ItemStack.EMPTY;
	}

	@Override
	public FluidStack getFluidInSlot()
	{
		return this.slot.getStack();
	}

	@Override
	public boolean getHasStack()
	{
		if( this.slot.hasPower() )
		{
			return this.getAEStack() != null;
		}
		return false;
	}

	@Override
	public void putStack( final ItemStack par1ItemStack )
	{

	}

	@Override
	public int getSlotStackLimit()
	{
		return 0;
	}

	@Nonnull
	@Override
	public ItemStack decrStackSize( final int par1 )
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isHere( final IInventory inv, final int slotIn )
	{
		return false;
	}

	@Override
	public boolean canTakeStack( final EntityPlayer par1EntityPlayer )
	{
		return false;
	}
}
