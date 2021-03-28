/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2020, AlgorithmX2, All rights reserved.
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

package appeng.util.item;

import appeng.api.storage.data.IAEItemStack;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This iterator will only return items from a collection that are meaningful (w.r.t. {@link IAEItemStack#isMeaningful()}.
 * Items that are not meaningful are automatically removed from the collection as it is being iterated.
 */
public class MeaningfulItemIterator<T extends IAEItemStack> implements Iterator<T> {
    private final Iterator<T> parent;
    private T next;

    public MeaningfulItemIterator(final Collection<T> collection) {
        this.parent = collection.iterator();
        this.next = seekNext();
    }

    @Override
    public boolean hasNext() {
        return this.next != null;
    }

    @Override
    public T next() {
        if (this.next == null) {
            throw new NoSuchElementException();
        }

        T result = this.next;
        this.next = this.seekNext();
        return result;
    }

    private T seekNext() {
        while (this.parent.hasNext()) {
            T item = this.parent.next();

            if (item.isMeaningful()) {
                return item;
            } else {
                this.parent.remove();
            }
        }

        return null;
    }
}
