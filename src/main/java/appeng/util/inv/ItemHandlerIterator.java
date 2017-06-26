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

package appeng.util.inv;


import java.util.Iterator;
import java.util.NoSuchElementException;

import net.minecraftforge.items.IItemHandler;


class ItemHandlerIterator implements Iterator<ItemSlot>
{

	private final IItemHandler itemHandler;

	private final ItemSlot itemSlot = new ItemSlot();

	private int slot = 0;

	ItemHandlerIterator( IItemHandler itemHandler )
	{
		this.itemHandler = itemHandler;
	}

	@Override
	public boolean hasNext()
	{
		return slot < itemHandler.getSlots();
	}

	@Override
	public ItemSlot next()
	{
		if( slot >= itemHandler.getSlots() )
		{
			throw new NoSuchElementException();
		}
		itemSlot.setExtractable( !itemHandler.extractItem( slot, 1, true ).isEmpty() );
		itemSlot.setItemStack( itemHandler.getStackInSlot( slot ) );
		itemSlot.setSlot( slot );
		slot++;
		return itemSlot;
	}

}
