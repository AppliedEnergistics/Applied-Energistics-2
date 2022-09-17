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

import java.util.Iterator;
import java.util.Map;


/**
 * Nested iterator for {@link appeng.me.MachineSet}
 * <p>
 * Traverses first over the {@link appeng.me.MachineSet} and then over every containing
 * {@link appeng.api.networking.IGridNode}
 */
public class GridNodeIterator implements Iterator<IGridNode> {
    private final Iterator<MachineSet> outerIterator;
    private Iterator<IGridNode> innerIterator;

    public GridNodeIterator(final Map<Class<? extends IGridHost>, MachineSet> machines) {
        this.outerIterator = machines.values().iterator();
        this.innerHasNext();
    }

    private boolean innerHasNext() {
        final boolean hasNext = this.outerIterator.hasNext();

        if (hasNext) {
            final MachineSet nextElem = this.outerIterator.next();
            this.innerIterator = nextElem.iterator();
        }

        return hasNext;
    }

    @Override
    public boolean hasNext() {
        while (true) {
            if (this.innerIterator.hasNext()) {
                return true;
            } else if (!this.innerHasNext()) {
                return false;
            }
        }
    }

    @Override
    public IGridNode next() {
        return this.innerIterator.next();
    }

    @Override
    public void remove() {
        this.innerIterator.remove();
    }
}
