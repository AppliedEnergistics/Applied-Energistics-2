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

package appeng.me;


import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.IReadOnlyCollection;


public final class GridNodeCollection implements IReadOnlyCollection<IGridNode>
{
	private final Map<Class<? extends IGridHost>, MachineSet> machines;

	public GridNodeCollection( Map<Class<? extends IGridHost>, MachineSet> machines )
	{
		this.machines = machines;
	}

	@Override
	public final Iterator<IGridNode> iterator()
	{
		return new GridNodeIterator( this.machines );
	}

	@Override
	public final int size()
	{
		int size = 0;

		for( Set<IGridNode> o : this.machines.values() )
		{
			size += o.size();
		}

		return size;
	}

	@Override
	public final boolean isEmpty()
	{
		for( Set<IGridNode> o : this.machines.values() )
		{
			if( !o.isEmpty() )
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public final boolean contains( Object maybeGridNode )
	{
		final boolean doesContainNode;

		if( maybeGridNode instanceof IGridNode )
		{
			final IGridNode node = (IGridNode) maybeGridNode;
			IGridHost machine = node.getMachine();
			Class<? extends IGridHost> machineClass = machine.getClass();

			MachineSet machineSet = this.machines.get( machineClass );

			doesContainNode = machineSet != null && machineSet.contains( maybeGridNode );
		}
		else
		{
			doesContainNode = false;
		}

		return doesContainNode;
	}
}
