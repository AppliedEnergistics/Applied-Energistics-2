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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import appeng.me.cluster.IAECluster;
import appeng.me.cluster.MBCalculator;
import appeng.tile.spatial.SpatialPylonBlockEntity;

public class SpatialPylonCluster implements IAECluster {

    private final ServerLevel world;
    private final BlockPos boundsMin;
    private final BlockPos boundsMax;
    private final List<SpatialPylonBlockEntity> line = new ArrayList<>();
    private boolean isDestroyed = false;

    private Axis currentAxis = Axis.UNFORMED;
    private boolean isValid;

    public SpatialPylonCluster(ServerLevel world, BlockPos boundsMin, BlockPos boundsMax) {
        this.world = world;
        this.boundsMin = boundsMin.immutable();
        this.boundsMax = boundsMax.immutable();

        if (this.getBoundsMin().getX() != this.getBoundsMax().getX()) {
            this.setCurrentAxis(Axis.X);
        } else if (this.getBoundsMin().getY() != this.getBoundsMax().getY()) {
            this.setCurrentAxis(Axis.Y);
        } else if (this.getBoundsMin().getZ() != this.getBoundsMax().getZ()) {
            this.setCurrentAxis(Axis.Z);
        } else {
            this.setCurrentAxis(Axis.UNFORMED);
        }
    }

    @Override
    public void updateStatus(final boolean updateGrid) {
        for (final SpatialPylonBlockEntity r : this.getLine()) {
            r.recalculateDisplay();
        }
    }

    @Override
    public boolean isDestroyed() {
        return isDestroyed;
    }

    @Override
    public void destroy() {

        if (this.isDestroyed) {
            return;
        }
        this.isDestroyed = true;

        MBCalculator.setModificationInProgress(this);
        try {
            for (final SpatialPylonBlockEntity r : this.getLine()) {
                r.updateStatus(null);
            }
        } finally {
            MBCalculator.setModificationInProgress(null);
        }
    }

    @Override
    public Iterator<SpatialPylonBlockEntity> getTiles() {
        return this.getLine().iterator();
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

    public ServerLevel getWorld() {
        return world;
    }

    @Override
    public BlockPos getBoundsMax() {
        return this.boundsMax;
    }

    @Override
    public BlockPos getBoundsMin() {
        return this.boundsMin;
    }

    List<SpatialPylonBlockEntity> getLine() {
        return this.line;
    }

    public enum Axis {
        X, Y, Z, UNFORMED
    }
}
