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

package appeng.helpers;

import java.util.Iterator;

public class NonNullArrayIterator<E> implements Iterator<E> {

    private final E[] g;
    private int offset = 0;

    public NonNullArrayIterator(final E[] o) {
        this.g = o;
    }

    @Override
    public boolean hasNext() {
        while (this.offset < this.g.length && this.g[this.offset] == null) {
            this.offset++;
        }

        return this.offset != this.g.length;
    }

    @Override
    public E next() {
        final E result = this.g[this.offset];
        this.offset++;

        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
