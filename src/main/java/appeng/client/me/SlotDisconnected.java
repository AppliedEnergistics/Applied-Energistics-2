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


import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.container.slot.AppEngSlot;
import appeng.util.Platform;


public class SlotDisconnected extends AppEngSlot
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
	public boolean canTakeStack( final PlayerEntity par1PlayerEntity )
	{
		return false;
	}

	@Override
	public ItemStack getDisplayStack()
	{
		if( Platform.isClient() )
		{
			final ItemStack is = super.getStack();
			// FIXME if( !is.isEmpty() && is.getItem() instanceof ItemEncodedPattern )
			// FIXME {
			// FIXME 	final ItemEncodedPattern iep = (ItemEncodedPattern) is.getItem();
			// FIXME 	final ItemStack out = iep.getOutput( is );
			// FIXME 	if( !out.isEmpty() )
			// FIXME 	{
			// FIXME 		return out;
			// FIXME 	}
			// FIXME }
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
