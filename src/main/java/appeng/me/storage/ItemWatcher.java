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

package appeng.me.storage;


import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.storage.data.IAEStack;
import appeng.me.cache.GridStorageCache;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Maintain my interests, and a global watch list, they should always be fully synchronized.
 */
public class ItemWatcher implements IStackWatcher {

    private final GridStorageCache gsc;
    private final IStackWatcherHost myObject;
    private final Set<IAEStack> myInterests = new HashSet<>();

    public ItemWatcher(final GridStorageCache cache, final IStackWatcherHost host) {
        this.gsc = cache;
        this.myObject = host;
    }

    public IStackWatcherHost getHost() {
        return this.myObject;
    }

    @Override
    public boolean add(final IAEStack e) {
        if (this.myInterests.contains(e)) {
            return false;
        }

        return this.myInterests.add(e.copy()) && this.gsc.getInterestManager().put(e, this);
    }

    @Override
    public boolean remove(final IAEStack o) {
        return this.myInterests.remove(o) && this.gsc.getInterestManager().remove(o, this);
    }

    @Override
    public void reset() {
        final Iterator<IAEStack> i = this.myInterests.iterator();

        while (i.hasNext()) {
            this.gsc.getInterestManager().remove(i.next(), this);
            i.remove();
        }
    }
}
