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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import appeng.api.storage.data.IAEItemStack;

public class MeaningfulItemIterator<T extends IAEItemStack> implements Iterator<T> {

    private final Collection<T> collection;
    private final Iterator<T> parent;
    private T next;
    private final Collection<T> toRemove = new ArrayList<>();

    public MeaningfulItemIterator(final Collection<T> collection) {
        this.collection = collection;
        this.parent = collection.iterator();
    }

    @Override
    public boolean hasNext() {
        while (this.parent.hasNext()) {
            this.next = this.parent.next();

            if (this.next.isMeaningful()) {
                return true;
            } else {
                // TODO: Avoid if possible
                this.toRemove.add(this.next);
                // this.parent.remove(); // self cleaning :3
            }
        }

        // Cleanup afterwards to avoid CMEs
        this.toRemove.forEach(entry -> this.collection.remove(entry));

        this.next = null;
        return false;
    }

    @Override
    public T next() {
        if (this.next == null) {
            throw new NoSuchElementException();
        }

        return this.next;
    }

    @Override
    public void remove() {
        this.parent.remove();
    }
}
