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

package appeng.crafting;

import java.util.HashSet;
import java.util.Set;

import appeng.api.networking.crafting.ICraftingWatcher;
import appeng.api.networking.crafting.ICraftingWatcherNode;
import appeng.api.storage.data.AEKey;
import appeng.me.service.CraftingService;

/**
 * Maintain my interests, and a global watch list, they should always be fully synchronized.
 */
public class CraftingWatcher implements ICraftingWatcher {

    private final CraftingService service;
    private final ICraftingWatcherNode host;
    private final Set<AEKey> myInterests = new HashSet<>();

    public CraftingWatcher(final CraftingService service, final ICraftingWatcherNode host) {
        this.service = service;
        this.host = host;
    }

    public ICraftingWatcherNode getHost() {
        return this.host;
    }

    @Override
    public boolean add(AEKey what) {
        if (this.myInterests.contains(what)) {
            return false;
        }

        return this.myInterests.add(what) && this.service.getInterestManager().put(what, this);
    }

    @Override
    public boolean remove(AEKey what) {
        return this.myInterests.remove(what) && this.service.getInterestManager().remove(what, this);
    }

    @Override
    public void reset() {
        var i = this.myInterests.iterator();

        while (i.hasNext()) {
            this.service.getInterestManager().remove(i.next(), this);
            i.remove();
        }
    }
}
