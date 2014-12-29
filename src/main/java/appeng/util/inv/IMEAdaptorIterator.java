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

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public class IMEAdaptorIterator implements Iterator<ItemSlot>
{

	final Iterator<IAEItemStack> stack;
	final ItemSlot slot = new ItemSlot();
	int offset = 0;
	boolean hasNext;

	final IMEAdaptor parent;
	final int containerSize;

	public IMEAdaptorIterator(IMEAdaptor parent, IItemList<IAEItemStack> availableItems) {
		this.stack = availableItems.iterator();
		this.containerSize = parent.maxSlots;
		this.parent = parent;
	}

	@Override
	public boolean hasNext()
	{
		this.hasNext = this.stack.hasNext();
		return this.offset < this.containerSize || this.hasNext;
	}

	@Override
	public ItemSlot next()
	{
		this.slot.slot = this.offset++;
		this.slot.isExtractable=true;

		if ( this.parent.maxSlots < this.offset )
			this.parent.maxSlots = this.offset;

		if ( this.hasNext )
		{
			IAEItemStack item = this.stack.next();
			this.slot.setAEItemStack( item );
			return this.slot;
		}

		this.slot.setItemStack( null );
		return this.slot;
	}

	@Override
	public void remove()
	{
		throw new RuntimeException( "Not Implemented!" );
	}
}
