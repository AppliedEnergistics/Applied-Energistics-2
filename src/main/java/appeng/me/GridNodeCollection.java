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


import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.IReadOnlyCollection;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class GridNodeCollection implements IReadOnlyCollection<IGridNode> {
    private final Map<Class<? extends IGridHost>, MachineSet> machines;

    public GridNodeCollection(final Map<Class<? extends IGridHost>, MachineSet> machines) {
        this.machines = machines;
    }

    @Override
    public Iterator<IGridNode> iterator() {
        return new GridNodeIterator(this.machines);
    }

    @Override
    public int size() {
        int size = 0;

        for (final Set<IGridNode> o : this.machines.values()) {
            size += o.size();
        }

        return size;
    }

    @Override
    public boolean isEmpty() {
        for (final Set<IGridNode> o : this.machines.values()) {
            if (!o.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean contains(final Object maybeGridNode) {
        final boolean doesContainNode;

        if (maybeGridNode instanceof IGridNode) {
            final IGridNode node = (IGridNode) maybeGridNode;
            final IGridHost machine = node.getMachine();
            final Class<? extends IGridHost> machineClass = machine.getClass();

            final MachineSet machineSet = this.machines.get(machineClass);

            doesContainNode = machineSet != null && machineSet.contains(maybeGridNode);
        } else {
            doesContainNode = false;
        }

        return doesContainNode;
    }
}
