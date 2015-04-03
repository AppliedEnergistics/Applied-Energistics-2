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

package appeng.util.iterators;


import java.util.Iterator;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;


public class InvIterator implements Iterator<ItemStack>
{

	final IInventory inv;
	final int size;

	int x = 0;

	public InvIterator( IInventory i )
	{
		this.inv = i;
		this.size = this.inv.getSizeInventory();
	}

	@Override
	public boolean hasNext()
	{
		return this.x < this.size;
	}

	@Override
	public ItemStack next()
	{
		ItemStack result = this.inv.getStackInSlot( this.x );
		this.x++;
		return result;
	}

	@Override
	public void remove()
	{
		throw new RuntimeException( "no..." );
	}
}
