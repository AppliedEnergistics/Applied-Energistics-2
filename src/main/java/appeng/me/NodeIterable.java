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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import appeng.api.networking.IGridNode;
import appeng.api.util.IReadOnlyCollection;

public class NodeIterable<T> implements IReadOnlyCollection<T>
{

	final private HashMap<Class, Set<IGridNode>> Machines;

	public NodeIterable(HashMap<Class, Set<IGridNode>> Machines) {
		this.Machines = Machines;
	}

	@Override
	public Iterator<T> iterator()
	{
		return new NodeIterator( Machines );
	}

	@Override
	public int size()
	{
		int size = 0;

		for (Set<IGridNode> o : Machines.values())
			size += o.size();

		return size;
	}

	@Override
	public boolean isEmpty()
	{
		for (Set<IGridNode> o : Machines.values())
			if ( !o.isEmpty() )
				return false;

		return true;
	}

	@Override
	public boolean contains(Object node)
	{
		Class c = ((IGridNode) node).getMachine().getClass();

		Set<IGridNode> p = Machines.get( c );
		if ( p != null )
			return p.contains( node );

		return false;
	}
}
