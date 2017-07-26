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


import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.util.Lazy;
import appeng.util.helpers.ItemHandlerUtil;


public class WrapperLazyItemHandler implements IInternalItemHandler
{
	private final Lazy<IItemHandler> sourceHandler;

	public WrapperLazyItemHandler( Supplier<IItemHandler> source )
	{
		this.sourceHandler = new Lazy<>( source );
	}

	@Override
	public int getSlots()
	{
		return sourceHandler.get().getSlots();
	}

	@Override
	public ItemStack getStackInSlot( int slot )
	{
		return sourceHandler.get().getStackInSlot( slot );
	}

	@Override
	public ItemStack insertItem( int slot, ItemStack stack, boolean simulate )
	{
		return sourceHandler.get().insertItem( slot, stack, simulate );
	}

	@Override
	public ItemStack extractItem( int slot, int amount, boolean simulate )
	{
		return sourceHandler.get().extractItem( slot, amount, simulate );
	}

	@Override
	public int getSlotLimit( int slot )
	{
		return sourceHandler.get().getSlotLimit( slot );
	}

	@Override
	public void setStackInSlot( int slot, ItemStack stack )
	{
		ItemHandlerUtil.setStackInSlot( sourceHandler.get(), slot, stack );
	}

	@Override
	public boolean isItemValidForSlot( int slot, ItemStack stack )
	{
		return ItemHandlerUtil.isItemValidForSlot( sourceHandler.get(), slot, stack );
	}

	@Override
	public void markDirty( int slot )
	{
		ItemHandlerUtil.markDirty( sourceHandler.get(), slot );
	}
}
