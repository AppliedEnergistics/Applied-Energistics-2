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

package appeng.me.pathfinding;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.me.GridConnection;
import appeng.me.GridNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Calculation to assign channels starting from the controllers. Basically a BFS, with one step each tick.
 */
public class PathingCalculation {

    private boolean finished = false;
    private final IGrid grid;
    /**
     * Path items that are part of a multiblock that was already granted a channel.
     */
    private final Set<IPathItem> multiblocksWithChannel = new HashSet<>();
    /**
     * The BFS queues: all the path items that need to be visited on the next tick. Dense queue is prioritized to have
     * the behavior of dense cables extending the controller faces, then cables, then normal devices.
     */
    private List<IPathItem>[] queues = new List[] {
            new ArrayList<>(), // 0: dense cable queue
            new ArrayList<>(), // 1: normal cable queue
            new ArrayList<>() // 2: non-cable queue
    };
    /**
     * Path items that are either in the queue, or have been processed already.
     */
    private final Set<IPathItem> visited = new HashSet<>();
    /**
     * Tracks the total number of used channels.
     */
    private int channelsInUse = 0;
    /**
     * Tracks the total number of channels for each path item is using.
     */
    private int channelsByBlocks = 0;

    /**
     * Create a new pathing calculation from the passed grid.
     */
    public PathingCalculation(IGrid grid) {
        this.grid = grid;

        // Add every outgoing connection of the controllers (that doesn't point to another controller) to the list.
        for (var node : grid.getMachineNodes(ControllerBlockEntity.class)) {
            visited.add((IPathItem) node);
            for (var gcc : node.getConnections()) {
                var gc = (GridConnection) gcc;
                if (!(gc.getOtherSide(node).getOwner() instanceof ControllerBlockEntity)) {
                    enqueue(gc, 0);
                    gc.setControllerRoute((GridNode) node);
                }
            }
        }
    }

    private void enqueue(IPathItem pathItem, int queueIndex) {
        visited.add(pathItem);

        int possibleIndex;

        if (pathItem instanceof GridConnection) {
            // Grid connection does not have flags, allow any queue.
            possibleIndex = 0;
        } else if (pathItem.hasFlag(GridFlags.DENSE_CAPACITY)) {
            // Dense queue if possible.
            possibleIndex = 0;
        } else if (pathItem.hasFlag(GridFlags.PREFERRED)) {
            // Cable queue if possible.
            possibleIndex = 1;
        } else {
            possibleIndex = 2;
        }

        int index = Math.max(possibleIndex, queueIndex);
        queues[index].add(pathItem);
    }

    public void step() {
        // Keep processing dense queue as long as it's not empty.
        for (int i = 0; i < 3; ++i) {
            if (!queues[i].isEmpty()) {
                List<IPathItem> oldOpen = queues[i];
                queues[i] = new ArrayList<>();
                processQueue(oldOpen, i);
                return;
            }
        }

        // If we reach this, perform the final channel count update
        propagateAssignments();
        finished = true;
    }

    private void processQueue(List<IPathItem> oldOpen, int queueIndex) {
        for (IPathItem i : oldOpen) {
            for (IPathItem pi : i.getPossibleOptions()) {
                if (!this.visited.contains(pi)) {
                    // Set BFS parent.
                    pi.setControllerRoute(i);

                    if (pi.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
                        if (!this.multiblocksWithChannel.contains(pi)) {
                            // Try to use the channel along the path.
                            boolean worked = tryUseChannel((GridNode) pi);
                            if (worked) {
                                ((GridNode) pi).setConsumesChannel();
                            }

                            if (worked && pi.hasFlag(GridFlags.MULTIBLOCK)) {
                                var multiblock = ((IGridNode) pi).getService(IGridMultiblock.class);
                                if (multiblock != null) {
                                    var oni = multiblock.getMultiblockNodes();
                                    while (oni.hasNext()) {
                                        final IGridNode otherNodes = oni.next();
                                        if (otherNodes != pi) {
                                            this.multiblocksWithChannel.add((IPathItem) otherNodes);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    enqueue(pi, queueIndex);
                }
            }
        }
    }

    /**
     * Try to allocate a channel along the path from {@code start} to the controller.
     *
     * @return true if allocation was successful
     */
    private boolean tryUseChannel(GridNode start) {
        if (start.hasFlag(GridFlags.COMPRESSED_CHANNEL) && !start.getSubtreeAllowsCompressedChannels()) {
            // Don't send a compressed channel through this item.
            return false;
        }

        // Check that the allocation is possible.
        GridNode pi = start;
        while (pi != null) {
            if (!pi.canSupportMoreChannels()) {
                return false;
            }

            pi = pi.getHighestSimilarParent();
        }

        // Allocate the channel along the path.
        pi = start;
        while (pi != null) {
            pi.incrementChannelCount(1);
            pi = pi.getHighestSimilarParent();
        }

        channelsInUse++;
        return true;
    }

    private static final Object SUBTREE_END = new Object();

    /**
     * Propagates assignment to all nodes
     */
    private void propagateAssignments() {
        List<Object> stack = new ArrayList<>();

        for (var node : grid.getMachineNodes(ControllerBlockEntity.class)) {
            for (var gcc : node.getConnections()) {
                var gc = (GridConnection) gcc;
                if (!(gc.getOtherSide(node).getOwner() instanceof ControllerBlockEntity)) {
                    stack.add(gc);
                }
            }
        }

        while (!stack.isEmpty()) {
            Object current = stack.getLast();
            if (current == SUBTREE_END) {
                stack.removeLast();
                IPathItem item = (IPathItem) stack.removeLast();
                item.aggregateChildChannels();
                channelsByBlocks += item.getUsedChannelCount();
            } else {
                stack.add(SUBTREE_END);
                if (current instanceof GridConnection connection) {
                    if (connection.b().getControllerRoute() == current) {
                        stack.add(connection.b());
                    }
                } else {
                    for (var connection : ((GridNode) current).getConnections()) {
                        if (((GridConnection) connection).getControllerRoute() == current) {
                            stack.add(connection);
                        }
                    }
                }
            }
        }


        // Give a channel to all nodes that are a part of a multiblock that was given a channel before.
        for (var multiblockNode : multiblocksWithChannel) {
            multiblockNode.incrementChannelCount(1);
        }
        multiblocksWithChannel.clear();
    }


    public boolean isFinished() {
        return finished;
    }

    public int getChannelsInUse() {
        return channelsInUse;
    }

    public int getChannelsByBlocks() {
        return channelsByBlocks;
    }
}
