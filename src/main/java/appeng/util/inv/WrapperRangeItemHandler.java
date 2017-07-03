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


public class WrapperRangeItemHandler implements IInternalItemHandler
{
	private final IItemHandler compose;
	private final int minSlot;
	private final int maxSlot;

	public WrapperRangeItemHandler( IItemHandler compose, int minSlot, int maxSlotExclusive )
	{
		this.compose = compose;
		this.minSlot = minSlot;
		this.maxSlot = maxSlotExclusive;
	}

	@Override
	public int getSlots()
	{
		return maxSlot - minSlot;
	}

	@Override
	@Nonnull
	public ItemStack getStackInSlot( int slot )
	{
		if( checkSlot( slot ) )
		{
			return compose.getStackInSlot( slot + minSlot );
		}

		return ItemStack.EMPTY;
	}

	@Override
	@Nonnull
	public ItemStack insertItem( int slot, @Nonnull ItemStack stack, boolean simulate )
	{
		if( checkSlot( slot ) )
		{
			return compose.insertItem( slot + minSlot, stack, simulate );
		}

		return stack;
	}

	@Override
	@Nonnull
	public ItemStack extractItem( int slot, int amount, boolean simulate )
	{
		if( checkSlot( slot ) )
		{
			return compose.extractItem( slot + minSlot, amount, simulate );
		}

		return ItemStack.EMPTY;
	}

	@Override
	public void setStackInSlot( int slot, @Nonnull ItemStack stack )
	{
		if( checkSlot( slot ) )
		{
			ItemHandlerUtil.setStackInSlot( compose, slot + minSlot, stack );
		}
	}

	@Override
	public int getSlotLimit( int slot )
	{
		if( checkSlot( slot ) )
		{
			return compose.getSlotLimit( slot + minSlot );
		}

		return 0;
	}

	private boolean checkSlot( int localSlot )
	{
		return localSlot + minSlot < maxSlot;
	}

	@Override
	public boolean isItemValidForSlot( int slot, ItemStack stack )
	{
		if( checkSlot( slot ) )
		{
			return ItemHandlerUtil.isItemValidForSlot( compose, slot + minSlot, stack );
		}
		return false;
	}

	@Override
	public void markDirty( int slot )
	{
		if( checkSlot( slot ) )
		{
			ItemHandlerUtil.markDirty( compose, slot + minSlot );
		}
	}

}
