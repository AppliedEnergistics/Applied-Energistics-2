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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.features.Locatables;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.core.AELog;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.MBCalculator;
import appeng.me.service.helpers.ConnectionWrapper;
import appeng.util.iterators.ChainedIterator;

public class QuantumCluster implements IAECluster, IActionHost {

    private static final Set<QuantumCluster> ACTIVE_CLUSTERS = new HashSet<>();

    static {
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ACTIVE_CLUSTERS.clear();
        });
        ServerWorldEvents.UNLOAD.register((server, level) -> {
            var iteration = new ArrayList<>(ACTIVE_CLUSTERS);
            for (QuantumCluster activeCluster : iteration) {
                activeCluster.onUnload(level);
            }
        });
    }

    private final BlockPos boundsMin;
    private final BlockPos boundsMax;
    private boolean isDestroyed = false;
    private boolean updateStatus = true;
    private QuantumBridgeBlockEntity[] Ring;
    private boolean registered = false;
    private ConnectionWrapper connection;
    private long thisSide;
    private long otherSide;
    private QuantumBridgeBlockEntity center;

    public QuantumCluster(final BlockPos min, final BlockPos max) {
        this.boundsMin = min.immutable();
        this.boundsMax = max.immutable();
        this.setRing(new QuantumBridgeBlockEntity[8]);
    }

    private void onUnload(ServerLevel level) {
        if (this.center.getLevel() == level) {
            this.setUpdateStatus(false);
            this.destroy();
        }
    }

    @Override
    public void updateStatus(final boolean updateGrid) {

        final long qe = this.center.getQEFrequency();

        if (this.thisSide != qe && this.thisSide != -qe) {
            if (qe != 0) {
                if (this.thisSide != 0) {
                    Locatables.quantumNetworkBridges().unregister(center.getLevel(), getLocatableKey());
                }

                if (this.canUseNode(-qe)) {
                    this.otherSide = qe;
                    this.thisSide = -qe;
                } else if (this.canUseNode(qe)) {
                    this.thisSide = qe;
                    this.otherSide = -qe;
                }

                Locatables.quantumNetworkBridges().register(center.getLevel(), getLocatableKey(), this);
            } else {
                Locatables.quantumNetworkBridges().unregister(center.getLevel(), getLocatableKey());

                this.otherSide = 0;
                this.thisSide = 0;
            }
        }

        var myOtherSide = this.otherSide == 0 ? null
                : Locatables.quantumNetworkBridges().get(center.getLevel(), this.otherSide);

        boolean shutdown = false;

        if (myOtherSide instanceof QuantumCluster sideB) {
            var sideA = this;

            if (sideA.isActive() && sideB.isActive()) {
                if (this.connection != null && this.connection.getConnection() != null) {
                    final IGridNode a = this.connection.getConnection().a();
                    final IGridNode b = this.connection.getConnection().b();
                    final IGridNode sa = sideA.getNode();
                    final IGridNode sb = sideB.getNode();
                    if ((a == sa || b == sa) && (a == sb || b == sb)) {
                        return;
                    }
                }

                try {
                    if (sideA.connection != null && sideA.connection.getConnection() != null) {
                        sideA.connection.getConnection().destroy();
                        sideA.connection = new ConnectionWrapper(null);
                    }

                    if (sideB.connection != null && sideB.connection.getConnection() != null) {
                        sideB.connection.getConnection().destroy();
                        sideB.connection = new ConnectionWrapper(null);
                    }

                    sideA.connection = sideB.connection = new ConnectionWrapper(
                            AEApi.grid().createGridConnection(sideA.getNode(), sideB.getNode()));
                } catch (final FailedConnectionException e) {
                    // :(
                    AELog.debug(e);
                }
            } else {
                shutdown = true;
            }
        } else {
            shutdown = true;
        }

        if (shutdown && this.connection != null && this.connection.getConnection() != null) {
            this.connection.getConnection().destroy();
            this.connection.setConnection(null);
            this.connection = new ConnectionWrapper(null);
        }
    }

    private boolean canUseNode(final long qe) {
        var locatable = Locatables.quantumNetworkBridges().get(center.getLevel(), qe);
        if (locatable instanceof QuantumCluster qc) {
            var level = qc.center.getLevel();
            if (!qc.isDestroyed) {
                // In future versions, we might actually want to delay the entire registration
                // until the center
                // block entity begins ticking normally.
                if (level.hasChunkAt(qc.center.getBlockPos())) {
                    final Level cur = level.getServer().getLevel(level.dimension());

                    final BlockEntity te = level.getBlockEntity(qc.center.getBlockPos());
                    return te != qc.center || level != cur;
                } else {
                    AELog.warn("Found a registered QNB with serial %s whose chunk seems to be unloaded: %s", qe, qc);
                }
            }
        }
        return true;
    }

    private boolean isActive() {
        if (this.isDestroyed || !this.registered) {
            return false;
        }

        return this.center.isPowered() && this.hasQES();
    }

    private IGridNode getNode() {
        return this.center.getGridNode();
    }

    private boolean hasQES() {
        return this.thisSide != 0;
    }

    @Override
    public BlockPos getBoundsMin() {
        return boundsMin;
    }

    @Override
    public BlockPos getBoundsMax() {
        return boundsMax;
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
            if (this.registered) {
                ACTIVE_CLUSTERS.remove(this);
                this.registered = false;
            }

            if (this.thisSide != 0) {
                this.updateStatus(true);
                Locatables.quantumNetworkBridges().unregister(center.getLevel(), getLocatableKey());
            }

            this.center.updateStatus(null, (byte) -1, this.isUpdateStatus());

            for (final QuantumBridgeBlockEntity r : this.getRing()) {
                r.updateStatus(null, (byte) -1, this.isUpdateStatus());
            }

            this.center = null;
            this.setRing(new QuantumBridgeBlockEntity[8]);
        } finally {
            MBCalculator.setModificationInProgress(null);
        }
    }

    @Override
    public Iterator<QuantumBridgeBlockEntity> getBlockEntities() {
        return new ChainedIterator<>(this.getRing()[0], this.getRing()[1], this.getRing()[2], this.getRing()[3],
                this.getRing()[4], this.getRing()[5], this.getRing()[6], this.getRing()[7], this.center);
    }

    public boolean isCorner(final QuantumBridgeBlockEntity quantumBridge) {
        return this.getRing()[0] == quantumBridge || this.getRing()[2] == quantumBridge
                || this.getRing()[4] == quantumBridge || this.getRing()[6] == quantumBridge;
    }

    private long getLocatableKey() {
        return this.thisSide;
    }

    public QuantumBridgeBlockEntity getCenter() {
        return this.center;
    }

    void setCenter(final QuantumBridgeBlockEntity c) {
        this.registered = true;
        ACTIVE_CLUSTERS.add(this);
        this.center = c;
    }

    private boolean isUpdateStatus() {
        return this.updateStatus;
    }

    public void setUpdateStatus(final boolean updateStatus) {
        this.updateStatus = updateStatus;
    }

    QuantumBridgeBlockEntity[] getRing() {
        return this.Ring;
    }

    private void setRing(final QuantumBridgeBlockEntity[] ring) {
        this.Ring = ring;
    }

    @Override
    public String toString() {
        if (center == null) {
            return "QuantumCluster{no-center}";
        }

        Level level = center.getLevel();
        BlockPos pos = center.getBlockPos();

        return "QuantumCluster{" + level + "," + pos + "}";
    }

    @Nullable
    @Override
    public IGridNode getActionableNode() {
        return center.getMainNode().getNode();
    }

}
