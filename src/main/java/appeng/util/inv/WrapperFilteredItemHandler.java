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


import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.filter.IAEItemFilter;


public class WrapperFilteredItemHandler implements IInternalItemHandler
{
	private final IItemHandler handler;
	private final IAEItemFilter filter;

	public WrapperFilteredItemHandler( @Nonnull IItemHandler handler, @Nonnull IAEItemFilter filter )
	{
		this.handler = handler;
		this.filter = filter;
	}

	@Override
	public void setStackInSlot( int slot, ItemStack stack )
	{
		ItemHandlerUtil.setStackInSlot( handler, slot, stack );
	}

	@Override
	public int getSlots()
	{
		return handler.getSlots();
	}

	@Override
	public ItemStack getStackInSlot( int slot )
	{
		return handler.getStackInSlot( slot );
	}

	@Override
	public ItemStack insertItem( int slot, ItemStack stack, boolean simulate )
	{
		if( !filter.allowInsert( handler, slot, stack ) )
		{
			return stack;
		}

		return handler.insertItem( slot, stack, simulate );
	}

	@Override
	public ItemStack extractItem( int slot, int amount, boolean simulate )
	{
		if( !filter.allowExtract( handler, slot, amount ) )
		{
			return ItemStack.EMPTY;
		}

		return handler.extractItem( slot, amount, simulate );
	}

	@Override
	public int getSlotLimit( int slot )
	{
		return handler.getSlotLimit( slot );
	}

	@Override
	public boolean isItemValidForSlot( int slot, ItemStack stack )
	{
		if( !filter.allowInsert( handler, slot, stack ) )
		{
			return false;
		}
		return ItemHandlerUtil.isItemValidForSlot( handler, slot, stack );
	}

	@Override
	public void markDirty( int slot )
	{
		ItemHandlerUtil.markDirty( handler, slot );
	}
}
