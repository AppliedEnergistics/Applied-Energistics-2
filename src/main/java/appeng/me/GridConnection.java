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

package appeng.me;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.Direction;

import appeng.api.exceptions.ExistingConnectionException;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.exceptions.NullNodeConnectionException;
import appeng.api.exceptions.SecurityConnectionException;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.pathing.IPathingService;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.me.pathfinding.IPathItem;
import appeng.util.Platform;

public class GridConnection implements IGridConnection, IPathItem {

    private int channelData = 0;
    private Object visitorIterationNumber = null;
    private GridNode sideA;
    @Nullable
    private Direction fromAtoB;
    private GridNode sideB;

    private GridConnection(final GridNode aNode, final GridNode bNode, @Nullable Direction fromAtoB) {
        this.sideA = aNode;
        this.fromAtoB = fromAtoB;
        this.sideB = bNode;
    }

    private boolean isNetworkABetter(final GridNode a, final GridNode b) {
        return a.getMyGrid().getPriority() > b.getMyGrid().getPriority() || a.getMyGrid().size() > b.getMyGrid().size();
    }

    @Override
    public IGridNode getOtherSide(final IGridNode gridNode) {
        if (gridNode == this.sideA) {
            return this.sideB;
        }
        if (gridNode == this.sideB) {
            return this.sideA;
        }

        throw new IllegalArgumentException("The given grid node does not participate in this connection.");
    }

    @Override
    public Direction getDirection(final IGridNode side) {
        if (this.fromAtoB == null) {
            return null;
        }

        if (this.sideA == side) {
            return this.fromAtoB;
        } else {
            return this.fromAtoB.getOpposite();
        }
    }

    @Override
    public void destroy() {
        // a connection was destroyed RE-PATH!! (this is not done immediately)
        var p = this.sideA.getInternalGrid().getPathingService();
        p.repath();

        this.sideA.removeConnection(this);
        this.sideB.removeConnection(this);

        this.sideA.validateGrid();
        this.sideB.validateGrid();
    }

    @Override
    public IGridNode a() {
        return this.sideA;
    }

    @Override
    public IGridNode b() {
        return this.sideB;
    }

    @Override
    public boolean isInWorld() {
        return this.fromAtoB != null;
    }

    @Override
    public int getUsedChannels() {
        return this.channelData >> 8 & 0xff;
    }

    @Override
    public IPathItem getControllerRoute() {
        if (this.sideA.hasFlag(GridFlags.CANNOT_CARRY)) {
            return null;
        }
        return this.sideA;
    }

    @Override
    public void setControllerRoute(final IPathItem fast, final boolean zeroOut) {
        if (zeroOut) {
            this.channelData &= ~0xff;
        }

        if (this.sideB == fast) {
            final GridNode tmp = this.sideA;
            this.sideA = this.sideB;
            this.sideB = tmp;
            if (this.fromAtoB != null) {
                this.fromAtoB = this.fromAtoB.getOpposite();
            }
        }
    }

    @Override
    public boolean canSupportMoreChannels() {
        return this.getLastUsedChannels() < Channels.getMaxChannels();
    }

    @Override
    public Iterable<IPathItem> getPossibleOptions() {
        return ImmutableList.of((IPathItem) this.a(), (IPathItem) this.b());
    }

    @Override
    public void incrementChannelCount(final int usedChannels) {
        this.channelData += usedChannels;
    }

    @Override
    public boolean hasFlag(GridFlags flag) {
        return false;
    }

    @Override
    public void finalizeChannels() {
        if (this.getUsedChannels() != this.getLastUsedChannels()) {
            this.channelData &= 0xff;
            this.channelData |= this.channelData << 8;

            if (this.sideA.getInternalGrid() != null) {
                this.sideA.notifyStatusChange(IGridNodeListener.State.CHANNEL);
            }

            if (this.sideB.getInternalGrid() != null) {
                this.sideB.notifyStatusChange(IGridNodeListener.State.CHANNEL);
            }
        }
    }

    private int getLastUsedChannels() {
        return this.channelData & 0xff;
    }

    Object getVisitorIterationNumber() {
        return this.visitorIterationNumber;
    }

    void setVisitorIterationNumber(final Object visitorIterationNumber) {
        this.visitorIterationNumber = visitorIterationNumber;
    }

    public static GridConnection create(final IGridNode aNode, final IGridNode bNode,
            @Nullable Direction externalDirection)
            throws FailedConnectionException {
        if (aNode == null || bNode == null) {
            throw new NullNodeConnectionException();
        }

        final GridNode a = (GridNode) aNode;
        final GridNode b = (GridNode) bNode;

        if (a.hasConnection(b) || b.hasConnection(a)) {
            throw new ExistingConnectionException(String
                    .format("Connection between node [%s] and [%s] on [%s] already exists.", a, b, externalDirection));
        }

        if (!Platform.securityCheck(a, b)) {
            if (AEConfig.instance().isSecurityAuditLogEnabled()) {
                AELog.info("Security audit 1 failed at [%s] belonging to player [id=%d]", a, a.getOwningPlayerId());
                AELog.info("Security audit 2 failed at [%s] belonging to player [id=%d]", b, b.getOwningPlayerId());
            }

            throw new SecurityConnectionException();
        }

        // Create the actual connection
        final GridConnection connection = new GridConnection(a, b, externalDirection);

        // Update both nodes with the new connection.
        if (a.getMyGrid() == null) {
            b.setGrid(a.getInternalGrid());
        } else if (a.getMyGrid() == null) {
            final GridPropagator gp = new GridPropagator(b.getInternalGrid());
            aNode.beginVisit(gp);
        } else if (b.getMyGrid() == null) {
            final GridPropagator gp = new GridPropagator(a.getInternalGrid());
            bNode.beginVisit(gp);
        } else if (connection.isNetworkABetter(a, b)) {
            final GridPropagator gp = new GridPropagator(a.getInternalGrid());
            b.beginVisit(gp);
        } else {
            final GridPropagator gp = new GridPropagator(b.getInternalGrid());
            a.beginVisit(gp);
        }

        // a connection was destroyed RE-PATH!!
        final IPathingService p = connection.sideA.getInternalGrid().getPathingService();
        p.repath();

        connection.sideA.addConnection(connection);
        connection.sideB.addConnection(connection);

        return connection;
    }
}
