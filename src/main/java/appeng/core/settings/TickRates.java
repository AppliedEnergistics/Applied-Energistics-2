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

package appeng.core.settings;

public enum TickRates {

    Interface(5, 120),

    ImportBus(5, 40),

    ExportBus(5, 60),

    AnnihilationPlane(2, 120),

    METunnel(5, 20),

    Inscriber(1, 20),

    Charger(10, 10),

    IOPort(1, 5),

    VibrationChamber(10, 40),

    StorageBus(5, 60),

    ItemTunnel(5, 60),

    LightTunnel(5, 60);

    private final int defaultMin;
    private final int defaultMax;
    private int min;
    private int max;

    TickRates(int min, int max) {
        this.defaultMin = min;
        this.defaultMax = max;
        this.min = min;
        this.max = max;
    }

    public int getDefaultMin() {
        return defaultMin;
    }

    public int getDefaultMax() {
        return defaultMax;
    }

    public int getMax() {
        return this.max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return this.min;
    }

    public void setMin(int min) {
        this.min = min;
    }

}
