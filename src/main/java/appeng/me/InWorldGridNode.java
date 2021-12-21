/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.exceptions.FailedConnectionException;
import appeng.api.exceptions.SecurityConnectionException;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.util.AEColor;
import appeng.core.AELog;
import appeng.hooks.ticking.TickHandler;

/**
 * A grid node that is accessible from within the level will also look actively for connections to nodes that are
 * adjacent in the level.
 */
public class InWorldGridNode extends GridNode {

    private final BlockPos location;

    public <T> InWorldGridNode(ServerLevel level,
            BlockPos location,
            T owner,
            @Nonnull IGridNodeListener<T> listener,
            Set<GridFlags> flags) {
        super(level, owner, listener, flags);
        this.location = location;
    }

    @Override
    protected void findInWorldConnections() {
        final EnumSet<Direction> newSecurityConnections = EnumSet.noneOf(Direction.class);

        // Clean up any connections that we might have left over to nodes that we can no longer reach
        cleanupConnections();

        // Find adjacent nodes in the level based on the sides of the host this node is exposed on
        var pos = new MutableBlockPos();
        sides: for (var direction : exposedOnSides) {
            pos.setWithOffset(location, direction);
            var adjacentNode = (GridNode) GridHelper.getExposedNode(getLevel(), pos, direction.getOpposite());
            if (adjacentNode == null) {
                continue;
            }

            // It is implied that the other node is exposed on the side since the host did return it for the side
            // so the only remaining condition is that the grid colors are compatible
            if (!hasCompatibleColor(adjacentNode)) {
                continue;
            }

            // Clean up phantom node connections for this side, if applicable
            for (var c : this.connections) {
                if (c.isInWorld() && c.getDirection(this) == direction) {
                    // This can essentially only occur if the adjacent node has changed, but the previous node has
                    // not properly severed their connection
                    var os = c.getOtherSide(this);
                    if (os == adjacentNode) {
                        // Keep the existing connection and carry on
                        continue sides;
                    } else {
                        AELog.warn("Grid node %s did not disconnect properly and is now replaced with %s",
                                os, adjacentNode);
                        c.destroy();
                    }
                    break;
                }
            }

            if (adjacentNode.getLastSecurityKey() != -1) {
                newSecurityConnections.add(direction);
            } else {
                if (!connectTo(direction, adjacentNode)) {
                    return;
                }
            }
        }

        for (var direction : newSecurityConnections) {
            pos.setWithOffset(location, direction);
            var adjacentNode = (GridNode) GridHelper.getExposedNode(getLevel(), pos, direction.getOpposite());
            if (adjacentNode == null) {
                continue;
            }

            if (!connectTo(direction, adjacentNode)) {
                return;
            }
        }
    }

    @Override
    public String toString() {
        return super.toString() + " @ " + location.getX() + "," + location.getY() + "," + location.getZ();
    }

    private void cleanupConnections() {
        // NOTE: this makes a defensive copy of the connections
        for (var connection : getConnections()) {
            if (!connection.isInWorld()) {
                continue; // Purely internal connections are never cleaned up
            }

            var ourSide = connection.getDirection(this);
            // If our external side is no longer exposed, the connection is invalid
            if (!isExposedOnSide(ourSide)) {
                connection.destroy();
                continue;
            }

            var theirSide = ourSide.getOpposite();
            IGridNode otherNode = connection.getOtherSide(this);
            if (!otherNode.isExposedOnSide(theirSide)) {
                connection.destroy();
                continue;
            }
        }
    }

    private boolean hasCompatibleColor(IGridNode otherNode) {
        var ourColor = getGridColor();
        var theirColor = otherNode.getGridColor();
        return ourColor == AEColor.TRANSPARENT || theirColor == AEColor.TRANSPARENT || ourColor == theirColor;
    }

    // construct a new connection between this and another nodes.
    private boolean connectTo(Direction direction, IGridNode adjacentNode) {
        try {
            GridConnection.create(adjacentNode, this, direction.getOpposite());
            return true;
        } catch (SecurityConnectionException e) {
            AELog.debug(e);
            TickHandler.instance().addCallable(
                    adjacentNode.getLevel(),
                    () -> callListener(IGridNodeListener::onSecurityBreak));

            return false;
        } catch (FailedConnectionException e) {
            AELog.debug(e);

            return false;
        }
    }

    public BlockPos getLocation() {
        return location;
    }
}
