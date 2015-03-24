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


public class ChainedIterator<T> implements Iterator<T>
{

	final T[] list;
	int offset = 0;

	public ChainedIterator( T... list )
	{
		this.list = list;
	}

	@Override
	public boolean hasNext()
	{
		return this.offset < this.list.length;
	}

	@Override
	public T next()
	{
		return this.list[this.offset++];
	}

	@Override
	public void remove()
	{
		throw new RuntimeException( "Not implemented." );
	}
}
