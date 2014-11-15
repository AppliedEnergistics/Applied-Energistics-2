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

package appeng.me.cache.helpers;

import java.util.Collection;
import java.util.Iterator;

import appeng.parts.p2p.PartP2PTunnel;

public class TunnelIterator<T extends PartP2PTunnel> implements Iterator<T>
{

	final Iterator<T> wrapped;
	final Class targetType;
	T Next;

	private void findNext()
	{
		while (Next == null && wrapped.hasNext())
		{
			Next = wrapped.next();
			if ( !targetType.isInstance( Next ) )
				Next = null;
		}
	}

	public TunnelIterator(Collection<T> tunnelSources, Class clz) {
		wrapped = tunnelSources.iterator();
		targetType = clz;
		findNext();
	}

	@Override
	public boolean hasNext()
	{
		findNext();
		return Next != null;
	}

	@Override
	public T next()
	{
		T tmp = Next;
		Next = null;
		return tmp;
	}

	@Override
	public void remove()
	{
		// no.
	}

}
