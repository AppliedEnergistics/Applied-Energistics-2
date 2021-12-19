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

import java.util.Objects;

import javax.annotation.Nullable;

import appeng.api.networking.pathing.ChannelMode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import net.minecraft.core.Direction;

import appeng.api.exceptions.ExistingConnectionException;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.exceptions.SecurityConnectionException;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.me.pathfinding.IPathItem;
import appeng.util.Platform;

public class GridConnection implements IGridConnection, IPathItem {

    private int usedChannels = 0;
    private int lastUsedChannels = 0;
    private Object visitorIterationNumber = null;
    /**
     * Note that in grids with a controller, following this side will always lead down the closest path towards the
     * controller.
     */
    private GridNode sideA;
    @Nullable
    private Direction fromAtoB;
    private GridNode sideB;

    private GridConnection(final GridNode aNode, final GridNode bNode, @Nullable Direction fromAtoB) {
        this.sideA = aNode;
        this.fromAtoB = fromAtoB;
        this.sideB = bNode;
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
        return usedChannels;
    }

    @Override
    public IPathItem getControllerRoute() {
        if (this.sideA.hasFlag(GridFlags.CANNOT_CARRY)) {
            return null;
        }
        return this.sideA;
    }

    @Override
    public void setControllerRoute(IPathItem fast, boolean zeroOut) {
        if (zeroOut) {
            this.lastUsedChannels = 0;
        }

        // If the shortest route to the controller is via side B, we need to flip the
        // connections sides because side A should be the closest route to the controller.
        if (this.sideB == fast) {
            var tmp = this.sideA;
            this.sideA = this.sideB;
            this.sideB = tmp;
            if (this.fromAtoB != null) {
                this.fromAtoB = this.fromAtoB.getOpposite();
            }
        }
    }

    @Override
    public boolean canSupportMoreChannels() {
        var mode = sideA.getGrid().getPathingService().getChannelMode();
        return this.getLastUsedChannels() < getMaxChannels();
    }

    @Override
    public int getMaxChannels() {
        return 32 * sideB.getGrid().getPathingService().getChannelMode().getCableCapacityFactor();
    }

    @Override
    public Iterable<IPathItem> getPossibleOptions() {
        return ImmutableList.of((IPathItem) this.a(), (IPathItem) this.b());
    }

    @Override
    public void incrementChannelCount(final int usedChannels) {
        this.lastUsedChannels += usedChannels;
    }

    @Override
    public boolean hasFlag(GridFlags flag) {
        return false;
    }

    @Override
    public void finalizeChannels() {
        if (this.getUsedChannels() != this.getLastUsedChannels()) {
            this.usedChannels = this.lastUsedChannels;

            if (this.sideA.getInternalGrid() != null) {
                this.sideA.notifyStatusChange(IGridNodeListener.State.CHANNEL);
            }

            if (this.sideB.getInternalGrid() != null) {
                this.sideB.notifyStatusChange(IGridNodeListener.State.CHANNEL);
            }
        }
    }

    private int getLastUsedChannels() {
        return this.lastUsedChannels;
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
        Objects.requireNonNull(aNode, "aNode");
        Objects.requireNonNull(bNode, "bNode");
        Preconditions.checkArgument(aNode != bNode, "Cannot connect node to itself");

        var a = (GridNode) aNode;
        var b = (GridNode) bNode;

        if (a.hasConnection(b) || b.hasConnection(a)) {
            throw new ExistingConnectionException(String
                    .format("Connection between node [%s] and [%s] on [%s] already exists.", a, b, externalDirection));
        }

        if (!Platform.securityCheck(a, b)) {
            throw new SecurityConnectionException();
        }

        // Create the actual connection
        var connection = new GridConnection(a, b, externalDirection);

        mergeGrids(a, b);

        // a connection was destroyed RE-PATH!!
        var p = connection.sideA.getInternalGrid().getPathingService();
        p.repath();

        connection.sideA.addConnection(connection);
        connection.sideB.addConnection(connection);

        return connection;
    }

    /**
     * Merge the grids of two grid nodes based on both becoming connected. This method assumes that the new connection
     * is NOT yet created, otherwise grid propagation will do more work than needed.
     */
    private static void mergeGrids(GridNode a, GridNode b) {
        // Update both nodes with the new connection.
        var gridA = a.getMyGrid();
        var gridB = b.getMyGrid();
        if (gridA == null && gridB == null) {
            // Neither A nor B has a grid, create a new grid spanning both
            assertNodeIsStandalone(a);
            assertNodeIsStandalone(b);
            var grid = Grid.create(a);
            a.setGrid(grid);
            b.setGrid(grid);
        } else if (gridA == null) {
            // Only node B has a grid, propagate it to A
            assertNodeIsStandalone(a);
            a.setGrid(gridB);
        } else if (gridB == null) {
            // Only node A has a grid, propagate it to B
            assertNodeIsStandalone(b);
            b.setGrid(gridA);
        } else if (gridA != gridB) {
            if (isGridABetterThanGridB(gridA, gridB)) {
                // Both A and B have grids, but A's grid is "better" -> propagate it to B and all its connected nodes
                var gp = new GridPropagator(a.getInternalGrid());
                b.beginVisit(gp);
            } else {
                // Both A and B have grids, but B's grid is "better" -> propagate it to A and all its connected nodes
                var gp = new GridPropagator(b.getInternalGrid());
                a.beginVisit(gp);
            }
        }
    }

    private static boolean isGridABetterThanGridB(Grid gridA, Grid gridB) {
        if (gridA.getPriority() != gridB.getPriority()) {
            return gridA.getPriority() > gridB.getPriority();
        }
        return gridA.size() >= gridB.size();
    }

    private static void assertNodeIsStandalone(GridNode node) {
        if (!node.hasNoConnections()) {
            throw new IllegalStateException("Grid node " + node + " has no grid, but is connected: "
                    + node.getConnections());
        }
    }
}
