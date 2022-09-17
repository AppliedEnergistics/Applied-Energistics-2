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


public class EnergyThreshold implements Comparable<EnergyThreshold> {

    private final double threshold;
    private final IEnergyWatcher watcher;
    private final int watcherHash;

    public EnergyThreshold(final double lim, final IEnergyWatcher watcher) {
        this.threshold = lim;
        this.watcher = watcher;
        this.watcherHash = watcher.hashCode();
    }

    /**
     * Special constructor to allow querying a for a subset of thresholds.
     *
     * @param lim
     * @param bound
     */
    public EnergyThreshold(final double lim, final int bound) {
        this.threshold = lim;
        this.watcher = null;
        this.watcherHash = bound;
    }

    public IEnergyWatcher getEnergyWatcher() {
        return this.watcher;
    }

    @Override
    public int compareTo(EnergyThreshold o) {
        int a = Double.compare(this.threshold, o.threshold);

        if (a == 0) {
            return Integer.compare(this.watcherHash, o.watcherHash);
        }

        return a;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(this.threshold);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((this.watcher == null) ? 0 : this.watcher.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }

        EnergyThreshold other = (EnergyThreshold) obj;
        if (Double.doubleToLongBits(this.threshold) != Double.doubleToLongBits(other.threshold)) {
            return false;
        }

        if (this.watcher == null) {
            return other.watcher == null;
        } else return this.watcher.equals(other.watcher);
    }
}
