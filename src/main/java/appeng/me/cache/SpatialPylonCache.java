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

package appeng.me.cache;


import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.spatial.ISpatialCache;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IReadOnlyCollection;
import appeng.core.AEConfig;
import appeng.me.cluster.implementations.SpatialPylonCluster;
import appeng.tile.spatial.TileSpatialIOPort;
import appeng.tile.spatial.TileSpatialPylon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SpatialPylonCache implements ISpatialCache {

    private final IGrid myGrid;
    private long powerRequired = 0;
    private double efficiency = 0.0;
    private DimensionalCoord captureMin;
    private DimensionalCoord captureMax;
    private boolean isValid = false;
    private List<TileSpatialIOPort> ioPorts = new ArrayList<>();
    private HashMap<SpatialPylonCluster, SpatialPylonCluster> clusters = new HashMap<>();

    public SpatialPylonCache(final IGrid g) {
        this.myGrid = g;
    }

    @MENetworkEventSubscribe
    public void bootingRender(final MENetworkBootingStatusChange c) {
        this.reset(this.myGrid);
    }

    private void reset(final IGrid grid) {

        this.clusters = new HashMap<>();
        this.ioPorts = new ArrayList<>();

        for (final IGridNode gm : grid.getMachines(TileSpatialIOPort.class)) {
            this.ioPorts.add((TileSpatialIOPort) gm.getMachine());
        }

        final IReadOnlyCollection<IGridNode> set = grid.getMachines(TileSpatialPylon.class);
        for (final IGridNode gm : set) {
            if (gm.meetsChannelRequirements()) {
                final SpatialPylonCluster c = ((TileSpatialPylon) gm.getMachine()).getCluster();
                if (c != null) {
                    this.clusters.put(c, c);
                }
            }
        }

        this.captureMax = null;
        this.captureMin = null;
        this.isValid = true;

        int pylonBlocks = 0;
        for (final SpatialPylonCluster cl : this.clusters.values()) {
            if (this.captureMax == null) {
                this.captureMax = cl.getMax().copy();
            }
            if (this.captureMin == null) {
                this.captureMin = cl.getMin().copy();
            }

            pylonBlocks += cl.tileCount();

            this.captureMin.x = Math.min(this.captureMin.x, cl.getMin().x);
            this.captureMin.y = Math.min(this.captureMin.y, cl.getMin().y);
            this.captureMin.z = Math.min(this.captureMin.z, cl.getMin().z);

            this.captureMax.x = Math.max(this.captureMax.x, cl.getMax().x);
            this.captureMax.y = Math.max(this.captureMax.y, cl.getMax().y);
            this.captureMax.z = Math.max(this.captureMax.z, cl.getMax().z);
        }

        double maxPower = 0;
        double minPower = 0;
        if (this.hasRegion()) {
            this.isValid = this.captureMax.x - this.captureMin.x > 1 && this.captureMax.y - this.captureMin.y > 1 && this.captureMax.z - this.captureMin.z > 1;

            for (final SpatialPylonCluster cl : this.clusters.values()) {
                switch (cl.getCurrentAxis()) {
                    case X:

                        this.isValid = this.isValid && ((this.captureMax.y == cl.getMin().y || this.captureMin.y == cl
                                .getMax().y) || (this.captureMax.z == cl.getMin().z || this.captureMin.z == cl.getMax().z)) && ((this.captureMax.y == cl
                                .getMax().y || this.captureMin.y == cl
                                .getMin().y) || (this.captureMax.z == cl.getMax().z || this.captureMin.z == cl.getMin().z));

                        break;
                    case Y:

                        this.isValid = this.isValid && ((this.captureMax.x == cl.getMin().x || this.captureMin.x == cl
                                .getMax().x) || (this.captureMax.z == cl.getMin().z || this.captureMin.z == cl.getMax().z)) && ((this.captureMax.x == cl
                                .getMax().x || this.captureMin.x == cl
                                .getMin().x) || (this.captureMax.z == cl.getMax().z || this.captureMin.z == cl.getMin().z));

                        break;
                    case Z:

                        this.isValid = this.isValid && ((this.captureMax.y == cl.getMin().y || this.captureMin.y == cl
                                .getMax().y) || (this.captureMax.x == cl.getMin().x || this.captureMin.x == cl.getMax().x)) && ((this.captureMax.y == cl
                                .getMax().y || this.captureMin.y == cl
                                .getMin().y) || (this.captureMax.x == cl.getMax().x || this.captureMin.x == cl.getMin().x));

                        break;
                    case UNFORMED:
                        this.isValid = false;
                        break;
                }
            }

            final int reqX = this.captureMax.x - this.captureMin.x;
            final int reqY = this.captureMax.y - this.captureMin.y;
            final int reqZ = this.captureMax.z - this.captureMin.z;
            final int requirePylonBlocks = Math.max(6, ((reqX * reqZ + reqX * reqY + reqY * reqZ) * 3) / 8);

            this.efficiency = (double) pylonBlocks / (double) requirePylonBlocks;

            if (this.efficiency > 1.0) {
                this.efficiency = 1.0;
            }
            if (this.efficiency < 0.0) {
                this.efficiency = 0.0;
            }

            minPower = (double) reqX * (double) reqY * reqZ * AEConfig.instance().getSpatialPowerMultiplier();
            maxPower = Math.pow(minPower, AEConfig.instance().getSpatialPowerExponent());
        }

        final double affective_efficiency = Math.pow(this.efficiency, 0.25);
        this.powerRequired = (long) (affective_efficiency * minPower + (1.0 - affective_efficiency) * maxPower);

        for (final SpatialPylonCluster cl : this.clusters.values()) {
            final boolean myWasValid = cl.isValid();
            cl.setValid(this.isValid);
            if (myWasValid != this.isValid) {
                cl.updateStatus(false);
            }
        }
    }

    @Override
    public boolean hasRegion() {
        return this.captureMin != null;
    }

    @Override
    public boolean isValidRegion() {
        return this.hasRegion() && this.isValid;
    }

    @Override
    public DimensionalCoord getMin() {
        return this.captureMin;
    }

    @Override
    public DimensionalCoord getMax() {
        return this.captureMax;
    }

    @Override
    public long requiredPower() {
        return this.powerRequired;
    }

    @Override
    public float currentEfficiency() {
        return (float) this.efficiency * 100;
    }

    @Override
    public void onUpdateTick() {
    }

    @Override
    public void removeNode(final IGridNode node, final IGridHost machine) {

    }

    @Override
    public void addNode(final IGridNode node, final IGridHost machine) {

    }

    @Override
    public void onSplit(final IGridStorage storageB) {

    }

    @Override
    public void onJoin(final IGridStorage storageB) {

    }

    @Override
    public void populateGridStorage(final IGridStorage storage) {

    }
}
