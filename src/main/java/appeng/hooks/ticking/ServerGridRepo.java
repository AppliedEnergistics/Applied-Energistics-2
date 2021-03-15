/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.hooks.ticking;

import java.util.HashSet;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import appeng.me.Grid;

/**
 * A class to hold data related to ticking networks.
 */
class ServerGridRepo {
    private Set<Grid> networks = new ObjectOpenHashSet<>();
    private Set<Grid> toAdd = new ObjectOpenHashSet<>();
    private Set<Grid> toRemove = new ObjectOpenHashSet<>();

    /**
     * Resets all internal data
     */
    void clear() {
        this.networks = new HashSet<>();
        this.toAdd = new HashSet<>();
        this.toRemove = new HashSet<>();
    }

    /**
     * Queues adding a new network.
     * <p>
     * Is added once {@link ServerGridRepo#updateNetworks()} is called.
     * <p>
     * Also removes it from the removal list, in case the network is validated again.
     */
    synchronized void addNetwork(Grid g) {
        this.toAdd.add(g);
        this.toRemove.remove(g);
    }

    /**
     * Queues removal of a network.
     * <p>
     * Is fully removed once {@link ServerGridRepo#updateNetworks()} is called.
     * <p>
     * Also removes it from the list to add in case it got invalid.
     */
    synchronized void removeNetwork(Grid g) {
        this.toRemove.add(g);
        this.toAdd.remove(g);
    }

    /**
     * Processes all networks to add or remove.
     * <p>
     * First all removals are handled, then the ones queued to be added.
     */
    synchronized void updateNetworks() {
        this.networks.removeAll(this.toRemove);
        this.toRemove.clear();

        this.networks.addAll(this.toAdd);
        this.toAdd.clear();
    }

    /**
     * Get all registered {@link Grid}s
     */
    public Set<Grid> getNetworks() {
        return networks;
    }

}
