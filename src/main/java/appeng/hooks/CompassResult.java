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

package appeng.hooks;


public class CompassResult {

    private final boolean hasResult;
    private final boolean spin;
    private final double rad;
    private final long time;
    private boolean requested = false;

    public CompassResult(final boolean hasResult, final boolean spin, final double rad) {
        this.hasResult = hasResult;
        this.spin = spin;
        this.rad = rad;
        this.time = System.currentTimeMillis();
    }

    public boolean isValidResult() {
        return this.hasResult;
    }

    public boolean isSpin() {
        return this.spin;
    }

    public double getRad() {
        return this.rad;
    }

    boolean isRequested() {
        return this.requested;
    }

    void setRequested(final boolean requested) {
        this.requested = requested;
    }

    long getTime() {
        return this.time;
    }
}
