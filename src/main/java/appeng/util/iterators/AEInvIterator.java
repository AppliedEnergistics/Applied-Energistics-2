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

import appeng.api.storage.data.IAEItemStack;
import appeng.tile.inventory.AppEngInternalAEInventory;

public class AEInvIterator implements Iterator<IAEItemStack>
{

	final AppEngInternalAEInventory inv;
	final int size;

	int x = 0;

	public AEInvIterator(AppEngInternalAEInventory i) {
		inv = i;
		size = inv.getSizeInventory();
	}

	@Override
	public boolean hasNext()
	{
		return x < size;
	}

	@Override
	public IAEItemStack next()
	{
		return inv.getAEStackInSlot( x++ );
	}

	@Override
	public void remove()
	{
		throw new RuntimeException( "no..." );
	}

}
