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


import appeng.api.AEApi;
import appeng.api.events.LocatableEventAnnounce;
import appeng.api.events.LocatableEventAnnounce.LocatableEvent;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.features.ILocatable;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.AEPartLocation;
import appeng.api.util.WorldCoord;
import appeng.core.AELog;
import appeng.me.cache.helpers.ConnectionWrapper;
import appeng.me.cluster.IAECluster;
import appeng.tile.qnb.TileQuantumBridge;
import appeng.util.iterators.ChainedIterator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Iterator;


public class QuantumCluster implements ILocatable, IAECluster {

    private final WorldCoord min;
    private final WorldCoord max;
    private boolean isDestroyed = false;
    private boolean updateStatus = true;
    private TileQuantumBridge[] Ring;
    private boolean registered = false;
    private ConnectionWrapper connection;
    private long thisSide;
    private long otherSide;
    private TileQuantumBridge center;

    public QuantumCluster(final WorldCoord min, final WorldCoord max) {
        this.min = min;
        this.max = max;
        this.setRing(new TileQuantumBridge[8]);
    }

    @SubscribeEvent
    public void onUnload(final WorldEvent.Unload e) {
        if (this.center.getWorld() == e.getWorld()) {
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

        final ILocatable myOtherSide = this.otherSide == 0 ? null : AEApi.instance().registries().locatable().getLocatableBy(this.otherSide);

        boolean shutdown = false;

        if (myOtherSide instanceof QuantumCluster) {
            final QuantumCluster sideA = this;
            final QuantumCluster sideB = (QuantumCluster) myOtherSide;

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
                    if (sideA.connection != null) {
                        if (sideA.connection.getConnection() != null) {
                            sideA.connection.getConnection().destroy();
                            sideA.connection = new ConnectionWrapper(null);
                        }
                    }

                    if (sideB.connection != null) {
                        if (sideB.connection.getConnection() != null) {
                            sideB.connection.getConnection().destroy();
                            sideB.connection = new ConnectionWrapper(null);
                        }
                    }

                    sideA.connection = sideB.connection = new ConnectionWrapper(AEApi.instance()
                            .grid()
                            .createGridConnection(sideA.getNode(),
                                    sideB.getNode()));
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

        if (shutdown && this.connection != null) {
            if (this.connection.getConnection() != null) {
                this.connection.getConnection().destroy();
                this.connection.setConnection(null);
                this.connection = new ConnectionWrapper(null);
            }
        }
    }

    private boolean canUseNode(final long qe) {
        final QuantumCluster qc = (QuantumCluster) AEApi.instance().registries().locatable().getLocatableBy(qe);
        if (qc != null) {
            final World theWorld = qc.center.getWorld();
            if (!qc.isDestroyed) {
                final Chunk c = theWorld.getChunkFromBlockCoords(qc.center.getPos());
                if (c.isLoaded()) {
                    final int id = theWorld.provider.getDimension();
                    final World cur = DimensionManager.getWorld(id);

                    final TileEntity te = theWorld.getTileEntity(qc.center.getPos());
                    return te != qc.center || theWorld != cur;
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
        return this.center.getGridNode(AEPartLocation.INTERNAL);
    }

    private boolean hasQES() {
        return this.thisSide != 0;
    }

    @Override
    public void destroy() {
        if (this.isDestroyed) {
            return;
        }
        this.isDestroyed = true;

        if (this.registered) {
            MinecraftForge.EVENT_BUS.unregister(this);
            this.registered = false;
        }

        if (this.thisSide != 0) {
            this.updateStatus(true);
            MinecraftForge.EVENT_BUS.post(new LocatableEventAnnounce(this, LocatableEvent.UNREGISTER));
        }

        this.center.updateStatus(null, (byte) -1, this.isUpdateStatus());

        for (final TileQuantumBridge r : this.getRing()) {
            r.updateStatus(null, (byte) -1, this.isUpdateStatus());
        }

        this.center = null;
        this.setRing(new TileQuantumBridge[8]);
    }

    @Override
    public Iterator<IGridHost> getTiles() {
        return new ChainedIterator<>(this.getRing()[0], this.getRing()[1], this.getRing()[2], this.getRing()[3], this.getRing()[4], this
                .getRing()[5], this.getRing()[6], this.getRing()[7], this.center);
    }

    public boolean isCorner(final TileQuantumBridge tileQuantumBridge) {
        return this.getRing()[0] == tileQuantumBridge || this.getRing()[2] == tileQuantumBridge || this.getRing()[4] == tileQuantumBridge || this
                .getRing()[6] == tileQuantumBridge;
    }

    @Override
    public long getLocatableSerial() {
        return this.thisSide;
    }

    public TileQuantumBridge getCenter() {
        return this.center;
    }

    void setCenter(final TileQuantumBridge c) {
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

    TileQuantumBridge[] getRing() {
        return this.Ring;
    }

    private void setRing(final TileQuantumBridge[] ring) {
        this.Ring = ring;
    }
}
