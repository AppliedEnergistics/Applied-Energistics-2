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

package appeng.me.energy;


import appeng.api.networking.energy.IEnergyWatcher;
import appeng.api.networking.energy.IEnergyWatcherHost;
import appeng.me.cache.EnergyGridCache;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Maintain my interests, and a global watch list, they should always be fully synchronized.
 */
public class EnergyWatcher implements IEnergyWatcher {

    private final EnergyGridCache gsc;
    private final IEnergyWatcherHost watcherHost;
    private final Set<EnergyThreshold> myInterests = new HashSet<>();

    public EnergyWatcher(final EnergyGridCache cache, final IEnergyWatcherHost host) {
        this.gsc = cache;
        this.watcherHost = host;
    }

    public void post(final EnergyGridCache energyGridCache) {
        this.watcherHost.onThresholdPass(energyGridCache);
    }

    public IEnergyWatcherHost getHost() {
        return this.watcherHost;
    }

    @Override
    public boolean add(final double amount) {
        final EnergyThreshold eh = new EnergyThreshold(amount, this);

        if (this.myInterests.contains(eh)) {
            return false;
        }

        return this.gsc.registerEnergyInterest(eh) && this.myInterests.add(eh);
    }

    @Override
    public boolean remove(final double amount) {
        final EnergyThreshold eh = new EnergyThreshold(amount, this);

        return this.myInterests.remove(eh) && this.gsc.unregisterEnergyInterest(eh);
    }

    @Override
    public void reset() {
        for (Iterator<EnergyThreshold> iterator = this.myInterests.iterator(); iterator.hasNext(); ) {
            final EnergyThreshold threshold = iterator.next();

            this.gsc.unregisterEnergyInterest(threshold);
            iterator.remove();
        }
    }

}
