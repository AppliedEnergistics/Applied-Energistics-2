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

package appeng.parts.automation;

import java.util.Iterator;

import scala.NotImplementedError;

public class NonNullArrayIterator<E> implements Iterator<E>
{

	int offset = 0;
	final E[] g;

	public NonNullArrayIterator(E[] o) {
		g = o;
	}

	@Override
	public boolean hasNext()
	{
		while (offset < g.length && g[offset] == null)
			offset++;

		return offset != g.length;
	}

	@Override
	public E next()
	{
		return g[offset++];
	}

	@Override
	public void remove()
	{
		throw new NotImplementedError();
	}

}
