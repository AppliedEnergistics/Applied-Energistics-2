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

package appeng.me.cluster.implementations;


import appeng.api.networking.IGridHost;
import appeng.api.util.DimensionalCoord;
import appeng.me.cluster.IAECluster;
import appeng.tile.spatial.TileSpatialPylon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class SpatialPylonCluster implements IAECluster {

    private final DimensionalCoord min;
    private final DimensionalCoord max;
    private final List<TileSpatialPylon> line = new ArrayList<>();
    private boolean isDestroyed = false;

    private Axis currentAxis = Axis.UNFORMED;
    private boolean isValid;

    public SpatialPylonCluster(final DimensionalCoord min, final DimensionalCoord max) {
        this.min = min.copy();
        this.max = max.copy();

        if (this.getMin().x != this.getMax().x) {
            this.setCurrentAxis(Axis.X);
        } else if (this.getMin().y != this.getMax().y) {
            this.setCurrentAxis(Axis.Y);
        } else if (this.getMin().z != this.getMax().z) {
            this.setCurrentAxis(Axis.Z);
        } else {
            this.setCurrentAxis(Axis.UNFORMED);
        }
    }

    @Override
    public void updateStatus(final boolean updateGrid) {
        for (final TileSpatialPylon r : this.getLine()) {
            r.recalculateDisplay();
        }
    }

    @Override
    public void destroy() {

        if (this.isDestroyed) {
            return;
        }
        this.isDestroyed = true;

        for (final TileSpatialPylon r : this.getLine()) {
            r.updateStatus(null);
        }
    }

    @Override
    public Iterator<IGridHost> getTiles() {
        return (Iterator) this.getLine().iterator();
    }

    public int tileCount() {
        return this.getLine().size();
    }

    public Axis getCurrentAxis() {
        return this.currentAxis;
    }

    private void setCurrentAxis(final Axis currentAxis) {
        this.currentAxis = currentAxis;
    }

    public boolean isValid() {
        return this.isValid;
    }

    public void setValid(final boolean isValid) {
        this.isValid = isValid;
    }

    public DimensionalCoord getMax() {
        return this.max;
    }

    public DimensionalCoord getMin() {
        return this.min;
    }

    List<TileSpatialPylon> getLine() {
        return this.line;
    }

    public enum Axis {
        X, Y, Z, UNFORMED
    }
}
