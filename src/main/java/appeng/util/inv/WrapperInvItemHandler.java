/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandler;

import appeng.util.helpers.ItemHandlerUtil;


public class WrapperInvItemHandler implements IInventory
{
	private final IItemHandler inv;

	public WrapperInvItemHandler( final IItemHandler inv )
	{
		this.inv = inv;
	}

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return null;
	}

	@Override
	public int getSizeInventory()
	{
		return inv.getSlots();
	}

	@Override
	public boolean isEmpty()
	{
		return ItemHandlerUtil.isEmpty( inv );
	}

	@Override
	public ItemStack getStackInSlot( int index )
	{
		return inv.getStackInSlot( index );
	}

	@Override
	public ItemStack decrStackSize( int index, int count )
	{
		return inv.extractItem( index, count, false );
	}

	@Override
	public ItemStack removeStackFromSlot( int index )
	{
		return inv.extractItem( index, inv.getSlotLimit( index ), false );
	}

	@Override
	public void setInventorySlotContents( int index, ItemStack stack )
	{
		ItemHandlerUtil.setStackInSlot( inv, index, stack );
	}

	@Override
	public int getInventoryStackLimit()
	{
		int max = 0;
		for( int i = 0; i < inv.getSlots(); ++i )
		{
			max = Math.max( max, inv.getSlotLimit( i ) );
		}
		return max;
	}

	@Override
	public void markDirty()
	{
		ItemHandlerUtil.markDirty( inv, -1 );
	}

	@Override
	public boolean isUsableByPlayer( EntityPlayer player )
	{
		return false;
	}

	@Override
	public void openInventory( EntityPlayer player )
	{
		// NOP
	}

	@Override
	public void closeInventory( EntityPlayer player )
	{
		// NOP
	}

	@Override
	public boolean isItemValidForSlot( int index, ItemStack stack )
	{
		return ItemHandlerUtil.isItemValidForSlot( inv, index, stack );
	}

	@Override
	public int getField( int id )
	{
		return 0;
	}

	@Override
	public void setField( int id, int value )
	{
		// NOP
	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		ItemHandlerUtil.clear( inv );
	}

}
