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

package appeng.me.helpers;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import appeng.api.stacks.AEKey;

public class InterestManager<T> {

    private final Multimap<AEKey, T> container;
    private final Set<T> allStacksWatchers = Sets.newIdentityHashSet();

    public InterestManager(Multimap<AEKey, T> interests) {
        this.container = interests;
    }

    public boolean put(final AEKey stack, final T iw) {
        return this.container.put(stack, iw);
    }

    public boolean remove(final AEKey stack, final T iw) {
        return this.container.remove(stack, iw);
    }

    public void setWatchAll(boolean watchAll, T watcher) {
        if (watchAll) {
            allStacksWatchers.add(watcher);
        } else {
            allStacksWatchers.remove(watcher);
        }
    }

    public boolean containsKey(final AEKey stack) {
        return this.container.containsKey(stack);
    }

    public Collection<T> get(final AEKey stack) {
        return this.container.get(stack);
    }

    public Collection<T> getAllStacksWatchers() {
        return this.allStacksWatchers;
    }
}
