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


import appeng.parts.p2p.PartP2PTunnel;

import java.util.Collection;
import java.util.Iterator;


public class TunnelIterator<T extends PartP2PTunnel> implements Iterator<T> {

    private final Iterator<T> wrapped;
    private final Class targetType;
    private T Next;

    public TunnelIterator(final Collection<T> tunnelSources, final Class clz) {
        this.wrapped = tunnelSources.iterator();
        this.targetType = clz;
        this.findNext();
    }

    private void findNext() {
        while (this.Next == null && this.wrapped.hasNext()) {
            this.Next = this.wrapped.next();
            if (!this.targetType.isInstance(this.Next)) {
                this.Next = null;
            }
        }
    }

    @Override
    public boolean hasNext() {
        this.findNext();
        return this.Next != null;
    }

    @Override
    public T next() {
        final T tmp = this.Next;
        this.Next = null;
        return tmp;
    }

    @Override
    public void remove() {
        // no.
    }
}
