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

import java.util.Iterator;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import appeng.api.events.LocatableEventAnnounce;
import appeng.api.events.LocatableEventAnnounce.LocatableEvent;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.features.ILocatable;
import appeng.api.networking.IGridNode;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.MBCalculator;
import appeng.me.service.helpers.ConnectionWrapper;
import appeng.tile.qnb.QuantumBridgeTileEntity;
import appeng.util.iterators.ChainedIterator;

public class QuantumCluster implements ILocatable, IAECluster {

    private final net.minecraft.core.BlockPos boundsMin;
    private final BlockPos boundsMax;
    private boolean isDestroyed = false;
    private boolean updateStatus = true;
    private QuantumBridgeTileEntity[] Ring;
    private boolean registered = false;
    private ConnectionWrapper connection;
    private long thisSide;
    private long otherSide;
    private QuantumBridgeTileEntity center;

    public QuantumCluster(final BlockPos min, final BlockPos max) {
        this.boundsMin = min.immutable();
        this.boundsMax = max.immutable();
        this.setRing(new QuantumBridgeTileEntity[8]);
    }

    @SubscribeEvent
    public void onUnload(final WorldEvent.Unload e) {
        if (this.center.getLevel() == e.getWorld()) {
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
                    MinecraftForge.EVENT_BUS.post(new LocatableEventAnnounce(this, LocatableEvent.UNREGISTER));
                }

                if (this.canUseNode(-qe)) {
                    this.otherSide = qe;
                    this.thisSide = -qe;
                } else if (this.canUseNode(qe)) {
                    this.thisSide = qe;
                    this.otherSide = -qe;
                }

                MinecraftForge.EVENT_BUS.post(new LocatableEventAnnounce(this, LocatableEvent.REGISTER));
            } else {
                MinecraftForge.EVENT_BUS.post(new LocatableEventAnnounce(this, LocatableEvent.UNREGISTER));

                this.otherSide = 0;
                this.thisSide = 0;
            }
        }

        final ILocatable myOtherSide = this.otherSide == 0 ? null
                : Api.instance().registries().locatable().getLocatableBy(this.otherSide);

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
                            Api.instance().grid().createGridConnection(sideA.getNode(), sideB.getNode()));
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
        final QuantumCluster qc = (QuantumCluster) Api.instance().registries().locatable().getLocatableBy(qe);
        if (qc != null) {
            final Level theWorld = qc.center.getLevel();
            if (!qc.isDestroyed) {
                // In future versions, we might actually want to delay the entire registration
                // until the center
                // tile begins ticking normally.
                if (theWorld.hasChunkAt(qc.center.getBlockPos())) {
                    final Level cur = theWorld.getServer().getLevel(theWorld.dimension());

                    final BlockEntity te = theWorld.getBlockEntity(qc.center.getBlockPos());
                    return te != qc.center || theWorld != cur;
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
    public net.minecraft.core.BlockPos getBoundsMin() {
        return boundsMin;
    }

    @Override
    public net.minecraft.core.BlockPos getBoundsMax() {
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
                MinecraftForge.EVENT_BUS.unregister(this);
                this.registered = false;
            }

            if (this.thisSide != 0) {
                this.updateStatus(true);
                MinecraftForge.EVENT_BUS.post(new LocatableEventAnnounce(this, LocatableEvent.UNREGISTER));
            }

            this.center.updateStatus(null, (byte) -1, this.isUpdateStatus());

            for (final QuantumBridgeTileEntity r : this.getRing()) {
                r.updateStatus(null, (byte) -1, this.isUpdateStatus());
            }

            this.center = null;
            this.setRing(new QuantumBridgeTileEntity[8]);
        } finally {
            MBCalculator.setModificationInProgress(null);
        }
    }

    @Override
    public Iterator<QuantumBridgeTileEntity> getTiles() {
        return new ChainedIterator<>(this.getRing()[0], this.getRing()[1], this.getRing()[2], this.getRing()[3],
                this.getRing()[4], this.getRing()[5], this.getRing()[6], this.getRing()[7], this.center);
    }

    public boolean isCorner(final QuantumBridgeTileEntity tileQuantumBridge) {
        return this.getRing()[0] == tileQuantumBridge || this.getRing()[2] == tileQuantumBridge
                || this.getRing()[4] == tileQuantumBridge || this.getRing()[6] == tileQuantumBridge;
    }

    @Override
    public long getLocatableSerial() {
        return this.thisSide;
    }

    public QuantumBridgeTileEntity getCenter() {
        return this.center;
    }

    void setCenter(final QuantumBridgeTileEntity c) {
        this.registered = true;
        MinecraftForge.EVENT_BUS.register(this);
        this.center = c;
    }

    private boolean isUpdateStatus() {
        return this.updateStatus;
    }

    public void setUpdateStatus(final boolean updateStatus) {
        this.updateStatus = updateStatus;
    }

    QuantumBridgeTileEntity[] getRing() {
        return this.Ring;
    }

    private void setRing(final QuantumBridgeTileEntity[] ring) {
        this.Ring = ring;
    }

    @Override
    public String toString() {
        if (center == null) {
            return "QuantumCluster{no-center}";
        }

        Level world = center.getLevel();
        BlockPos pos = center.getBlockPos();

        return "QuantumCluster{" + world + "," + pos + "}";
    }

}
