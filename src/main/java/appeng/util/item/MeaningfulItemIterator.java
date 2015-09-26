/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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
import java.util.NavigableMap;
import java.util.NoSuchElementException;

import appeng.api.storage.data.IAEItemStack;


public class MeaningfulItemIterator<T extends IAEItemStack> implements Iterator<T>
{

	private final Iterator<NavigableMap<T, T>> parent;
	private Iterator<T> innerIterater = null;
	private T next;

	public MeaningfulItemIterator( final Iterator<NavigableMap<T, T>> iterator )
	{
		this.parent = iterator;

		if( this.parent.hasNext() )
		{
			this.innerIterater = this.parent.next().values().iterator();
		}
	}

	@Override
	public boolean hasNext()
	{
		if( this.innerIterater == null )
		{
			return false;
		}

		while( this.innerIterater.hasNext() || this.parent.hasNext() )
		{
			if( this.innerIterater.hasNext() )
			{
				this.next = this.innerIterater.next();

				if( this.next.isMeaningful() )
				{
					return true;
				}
				else
				{
					this.innerIterater.remove(); // self cleaning :3
				}
			}

			if( this.parent.hasNext() )
			{
				this.innerIterater = this.parent.next().values().iterator();
			}
		}

		this.next = null;
		return false;
	}

	@Override
	public T next()
	{
		if( this.next == null )
		{
			throw new NoSuchElementException();
		}

		return this.next;
	}

	@Override
	public void remove()
	{
		this.parent.remove();
	}
}
