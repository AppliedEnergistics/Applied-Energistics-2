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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import appeng.api.networking.energy.IEnergyWatcher;
import appeng.api.networking.energy.IEnergyWatcherNode;
import appeng.me.service.EnergyService;

/**
 * Maintain my interests, and a global watch list, they should always be fully synchronized.
 */
public class EnergyWatcher implements IEnergyWatcher {

    private final EnergyService service;
    private final IEnergyWatcherNode watcherHost;
    private final Set<EnergyThreshold> myInterests = new HashSet<>();

    public EnergyWatcher(final EnergyService service, final IEnergyWatcherNode host) {
        this.service = service;
        this.watcherHost = host;
    }

    public void post(final EnergyService service) {
        this.watcherHost.onThresholdPass(service);
    }

    public IEnergyWatcherNode getHost() {
        return this.watcherHost;
    }

    @Override
    public boolean add(final double amount) {
        final EnergyThreshold eh = new EnergyThreshold(amount, this);

        if (this.myInterests.contains(eh)) {
            return false;
        }

        return this.service.registerEnergyInterest(eh) && this.myInterests.add(eh);
    }

    @Override
    public boolean remove(final double amount) {
        final EnergyThreshold eh = new EnergyThreshold(amount, this);

        return this.myInterests.remove(eh) && this.service.unregisterEnergyInterest(eh);
    }

    @Override
    public void reset() {
        for (Iterator<EnergyThreshold> iterator = this.myInterests.iterator(); iterator.hasNext();) {
            final EnergyThreshold threshold = iterator.next();

            this.service.unregisterEnergyInterest(threshold);
            iterator.remove();
        }
    }

}
