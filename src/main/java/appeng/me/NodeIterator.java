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

package appeng.me;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class NodeIterator<IGridNode> implements Iterator<IGridNode>
{

	boolean hasMore;
	final Iterator lvl1;
	Iterator lvl2;

	boolean pull()
	{
		hasMore = lvl1.hasNext();
		if ( hasMore )
		{
			lvl2 = ((Collection) lvl1.next()).iterator();
			return true;
		}
		return false;
	}

	public NodeIterator(HashMap<Class, Set<IGridNode>> machines) {
		lvl1 = machines.values().iterator();
		pull();
	}

	@Override
	public boolean hasNext()
	{
		if ( lvl2.hasNext() )
			return true;
		if ( pull() )
			return hasNext();
		return hasMore;
	}

	@Override
	public IGridNode next()
	{
		return (IGridNode) lvl2.next();
	}

	@Override
	public void remove()
	{
		lvl2.remove();
	}

}
