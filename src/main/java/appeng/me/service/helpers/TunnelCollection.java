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

package appeng.me.service.helpers;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import appeng.parts.p2p.P2PTunnelPart;

public class TunnelCollection<T extends P2PTunnelPart> implements Iterable<T> {

    private final Class clz;
    private Collection<T> tunnelSources;

    public TunnelCollection(final Collection<T> src, final Class c) {
        this.tunnelSources = src;
        this.clz = c;
    }

    public void setSource(final Collection<T> c) {
        this.tunnelSources = c;
    }

    public boolean isEmpty() {
        return !this.iterator().hasNext();
    }

    @Override
    public Iterator<T> iterator() {
        if (this.tunnelSources == null) {
            return Collections.emptyIterator();
        }
        return new TunnelIterator<>(this.tunnelSources, this.clz);
    }

    public boolean matches(final Class<? extends P2PTunnelPart> c) {
        return this.clz == c;
    }

    public Class<? extends P2PTunnelPart> getClz() {
        return this.clz;
    }

    public int size() {
        return this.tunnelSources == null ? 0 : this.tunnelSources.size();
    }

}
