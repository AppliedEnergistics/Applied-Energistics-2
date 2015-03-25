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

package appeng.util.item;


import java.util.Iterator;

import appeng.api.storage.data.IAEStack;


public class MeaningfulIterator<StackType extends IAEStack> implements Iterator<StackType>
{

	private final Iterator<StackType> parent;
	private StackType next;

	public MeaningfulIterator( Iterator<StackType> iterator )
	{
		this.parent = iterator;
	}

	@Override
	public boolean hasNext()
	{
		while( this.parent.hasNext() )
		{
			this.next = this.parent.next();
			if( this.next.isMeaningful() )
			{
				return true;
			}
			else
			{
				this.parent.remove(); // self cleaning :3
			}
		}

		return false;
	}

	@Override
	public StackType next()
	{
		return this.next;
	}

	@Override
	public void remove()
	{
		this.parent.remove();
	}
}
