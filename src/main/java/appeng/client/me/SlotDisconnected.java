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


import appeng.container.slot.IJEITargetSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.container.slot.AppEngSlot;
import appeng.items.misc.ItemEncodedPattern;
import appeng.util.Platform;


public class SlotDisconnected extends AppEngSlot implements IJEITargetSlot
{

	private final ClientDCInternalInv mySlot;

	public SlotDisconnected( final ClientDCInternalInv me, final int which, final int x, final int y )
	{
		super( me.getInventory(), which, x, y );
		this.mySlot = me;
	}

	@Override
	public boolean isItemValid( final ItemStack par1ItemStack )
	{
		return false;
	}

	@Override
	public void putStack( final ItemStack par1ItemStack )
	{

	}

	@Override
	public boolean canTakeStack( final EntityPlayer par1EntityPlayer )
	{
		return false;
	}

	@Override
	public ItemStack getDisplayStack()
	{
		if( Platform.isClient() )
		{
			final ItemStack is = super.getStack();
			if( !is.isEmpty() && is.getItem() instanceof ItemEncodedPattern )
			{
				final ItemEncodedPattern iep = (ItemEncodedPattern) is.getItem();
				final ItemStack out = iep.getOutput( is );
				if( !out.isEmpty() )
				{
					return out;
				}
			}
		}
		return super.getStack();
	}

	@Override
	public boolean getHasStack()
	{
		return !this.getStack().isEmpty();
	}

	@Override
	public int getSlotStackLimit()
	{
		return 0;
	}

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

	public ClientDCInternalInv getSlot()
	{
		return this.mySlot;
	}
}
