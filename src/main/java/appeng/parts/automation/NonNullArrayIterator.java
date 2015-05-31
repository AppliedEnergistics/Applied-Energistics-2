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

package appeng.parts.automation;


import java.util.Iterator;

import scala.NotImplementedError;


public final class NonNullArrayIterator<E> implements Iterator<E>
{

	final E[] g;
	int offset = 0;

	public NonNullArrayIterator( E[] o )
	{
		this.g = o;
	}

	@Override
	public final boolean hasNext()
	{
		while( this.offset < this.g.length && this.g[this.offset] == null )
		{
			this.offset++;
		}

		return this.offset != this.g.length;
	}

	@Override
	public final E next()
	{
		E result = this.g[this.offset];
		this.offset++;
		return result;
	}

	@Override
	public final void remove()
	{
		throw new NotImplementedError();
	}
}
