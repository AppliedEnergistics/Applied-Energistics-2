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

package appeng.me.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.events.GridBootingStatusChange;
import appeng.api.networking.spatial.ISpatialService;
import appeng.blockentity.spatial.SpatialIOPortBlockEntity;
import appeng.blockentity.spatial.SpatialPylonBlockEntity;
import appeng.core.AEConfig;
import appeng.me.cluster.implementations.SpatialPylonCluster;

public class SpatialPylonService implements ISpatialService, IGridServiceProvider {

    static {
        GridHelper.addGridServiceEventHandler(GridBootingStatusChange.class, ISpatialService.class,
                (service, evt) -> {
                    ((SpatialPylonService) service).bootingRender(evt);
                });
    }

    private final IGrid myGrid;
    private long powerRequired = 0;
    private double efficiency = 0.0;
    private ServerLevel captureLevel;
    private BlockPos captureMin;
    private BlockPos captureMax;
    private boolean isValid = false;
    private List<SpatialIOPortBlockEntity> ioPorts = new ArrayList<>();
    private HashMap<SpatialPylonCluster, SpatialPylonCluster> clusters = new HashMap<>();

    public SpatialPylonService(IGrid g) {
        this.myGrid = g;
    }

    public void bootingRender(GridBootingStatusChange c) {
        this.reset(this.myGrid);
    }

    private void reset(IGrid grid) {

        this.clusters = new HashMap<>();
        this.ioPorts = new ArrayList<>();

        for (var gm : grid.getMachineNodes(SpatialIOPortBlockEntity.class)) {
            this.ioPorts.add((SpatialIOPortBlockEntity) gm.getOwner());
        }

        for (var gm : grid.getMachineNodes(SpatialPylonBlockEntity.class)) {
            if (gm.meetsChannelRequirements()) {
                final SpatialPylonCluster c = ((SpatialPylonBlockEntity) gm.getOwner()).getCluster();
                if (c != null) {
                    this.clusters.put(c, c);
                }
            }
        }

        this.captureLevel = null;
        this.isValid = true;

        MutableBlockPos minPoint = null;
        MutableBlockPos maxPoint = null;

        int pylonBlocks = 0;
        for (SpatialPylonCluster cl : this.clusters.values()) {
            if (this.captureLevel == null) {
                this.captureLevel = cl.setLevel();
            } else if (this.captureLevel != cl.setLevel()) {
                continue;
            }

            // Expand the bounding box
            if (maxPoint == null) {
                maxPoint = cl.getBoundsMax().mutable();
            } else {
                maxPoint.setX(Math.max(maxPoint.getX(), cl.getBoundsMax().getX()));
                maxPoint.setY(Math.max(maxPoint.getY(), cl.getBoundsMax().getY()));
                maxPoint.setZ(Math.max(maxPoint.getZ(), cl.getBoundsMax().getZ()));
            }

            if (minPoint == null) {
                minPoint = cl.getBoundsMin().mutable();
            } else {
                minPoint.setX(Math.min(minPoint.getX(), cl.getBoundsMin().getX()));
                minPoint.setY(Math.min(minPoint.getY(), cl.getBoundsMin().getY()));
                minPoint.setZ(Math.min(minPoint.getZ(), cl.getBoundsMin().getZ()));
            }

            pylonBlocks += cl.size();
        }

        this.captureMin = minPoint != null ? minPoint.immutable() : null;
        this.captureMax = maxPoint != null ? maxPoint.immutable() : null;

        double minPower = 0;
        if (this.hasRegion()) {
            this.isValid = this.captureMax.getX() - this.captureMin.getX() > 1
                    && this.captureMax.getY() - this.captureMin.getY() > 1
                    && this.captureMax.getZ() - this.captureMin.getZ() > 1;

            for (SpatialPylonCluster cl : this.clusters.values()) {
                switch (cl.getCurrentAxis()) {
                    case X -> this.isValid = this.isValid
                            && (this.captureMax.getY() == cl.getBoundsMin().getY()
                                    || this.captureMin.getY() == cl.getBoundsMax().getY()
                                    || this.captureMax.getZ() == cl.getBoundsMin().getZ()
                                    || this.captureMin.getZ() == cl.getBoundsMax().getZ())
                            && (this.captureMax.getY() == cl.getBoundsMax().getY()
                                    || this.captureMin.getY() == cl.getBoundsMin().getY()
                                    || this.captureMax.getZ() == cl.getBoundsMax().getZ()
                                    || this.captureMin.getZ() == cl.getBoundsMin().getZ());
                    case Y -> this.isValid = this.isValid
                            && (this.captureMax.getX() == cl.getBoundsMin().getX()
                                    || this.captureMin.getX() == cl.getBoundsMax().getX()
                                    || this.captureMax.getZ() == cl.getBoundsMin().getZ()
                                    || this.captureMin.getZ() == cl.getBoundsMax().getZ())
                            && (this.captureMax.getX() == cl.getBoundsMax().getX()
                                    || this.captureMin.getX() == cl.getBoundsMin().getX()
                                    || this.captureMax.getZ() == cl.getBoundsMax().getZ()
                                    || this.captureMin.getZ() == cl.getBoundsMin().getZ());
                    case Z -> this.isValid = this.isValid
                            && (this.captureMax.getY() == cl.getBoundsMin().getY()
                                    || this.captureMin.getY() == cl.getBoundsMax().getY()
                                    || this.captureMax.getX() == cl.getBoundsMin().getX()
                                    || this.captureMin.getX() == cl.getBoundsMax().getX())
                            && (this.captureMax.getY() == cl.getBoundsMax().getY()
                                    || this.captureMin.getY() == cl.getBoundsMin().getY()
                                    || this.captureMax.getX() == cl.getBoundsMax().getX()
                                    || this.captureMin.getX() == cl.getBoundsMin().getX());
                    case UNFORMED -> this.isValid = false;
                }
            }

            final int reqX = this.captureMax.getX() - this.captureMin.getX();
            final int reqY = this.captureMax.getY() - this.captureMin.getY();
            final int reqZ = this.captureMax.getZ() - this.captureMin.getZ();
            final int requirePylonBlocks = Math.max(6, (reqX * reqZ + reqX * reqY + reqY * reqZ) * 3 / 8);

            this.efficiency = (double) pylonBlocks / (double) requirePylonBlocks;

            if (this.efficiency > 1.0) {
                this.efficiency = 1.0;
            }
            if (this.efficiency < 0.0) {
                this.efficiency = 0.0;
            }

            minPower = (double) reqX * (double) reqY * reqZ * AEConfig.instance().getSpatialPowerMultiplier();
        }

        this.powerRequired = (long) Math.pow(minPower,
                1 + (AEConfig.instance().getSpatialPowerExponent() - 1) * (1 - this.efficiency));

        for (SpatialPylonCluster cl : this.clusters.values()) {
            final boolean myWasValid = cl.isValid();
            cl.setValid(this.isValid);
            if (myWasValid != this.isValid) {
                cl.updateStatus(false);
            }
        }
    }

    @Override
    public boolean hasRegion() {
        return this.captureLevel != null && this.captureMin != null && this.captureMax != null;
    }

    @Override
    public boolean isValidRegion() {
        return this.hasRegion() && this.isValid;
    }

    @Override
    public ServerLevel getLevel() {
        return this.captureLevel;
    }

    @Override
    public BlockPos getMin() {
        return this.captureMin;
    }

    @Override
    public BlockPos getMax() {
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

}
