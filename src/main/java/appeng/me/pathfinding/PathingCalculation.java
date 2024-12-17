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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.me.GridConnection;
import appeng.me.GridNode;

/**
 * Calculation to assign channels starting from the controllers. The full computation is split in two steps, each linear
 * time.
 *
 * <p>
 * First, a BFS is performed starting from the controllers. This establishes a tree that connects all path items to a
 * controller. As nodes that require channels are visited, they are assigned a channel if possible. This is done by
 * checking the channel count of a few key nodes (max 3) along the path.
 *
 * <p>
 * Second, a DFS is performed to propagate the channel count upwards.
 */
public class PathingCalculation {
    private final IGrid grid;
    /**
     * Path items that are part of a multiblock that was already granted a channel.
     */
    private final Set<GridNode> multiblocksWithChannel = new HashSet<>();
    /**
     * The BFS queues: all the path items that need to be visited on the next tick. Dense queue is prioritized to have
     * the behavior of dense cables extending the controller faces, then cables, then normal devices.
     */
    private final Queue<IPathItem>[] queues = new Queue[] {
            new ArrayDeque<>(), // 0: dense cable queue
            new ArrayDeque<>(), // 1: normal cable queue
            new ArrayDeque<>() // 2: non-cable queue
    };
    /**
     * Path items that are either in a queue, or have been processed already.
     */
    private final Set<IPathItem> visited = new HashSet<>();
    /**
     * Tracks the number of channels assigned to each path item during the BFS pass. Only a few key nodes along any path
     * are checked and updated.
     */
    private final Reference2IntOpenHashMap<GridNode> channelBottlenecks = new Reference2IntOpenHashMap<>();
    /**
     * Nodes that have been granted a channel during the BFS pass.
     */
    private final Set<GridNode> channelNodes = new HashSet<>();
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

    public void compute() {
        // BFS pass
        for (int i = 0; i < 3; ++i) {
            processQueue(queues[i], i);
        }

        // DFS pass
        propagateAssignments();
    }

    private void processQueue(Queue<IPathItem> oldOpen, int queueIndex) {
        while (!oldOpen.isEmpty()) {
            IPathItem i = oldOpen.poll();
            for (IPathItem pi : i.getPossibleOptions()) {
                if (!this.visited.contains(pi)) {
                    // Set BFS parent.
                    pi.setControllerRoute(i);

                    if (pi.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
                        if (!this.multiblocksWithChannel.contains(pi)) {
                            // Try to use the channel along the path.
                            boolean worked = tryUseChannel((GridNode) pi);

                            if (worked && pi.hasFlag(GridFlags.MULTIBLOCK)) {
                                var multiblock = ((IGridNode) pi).getService(IGridMultiblock.class);
                                if (multiblock != null) {
                                    var oni = multiblock.getMultiblockNodes();
                                    while (oni.hasNext()) {
                                        final IGridNode otherNodes = oni.next();
                                        if (otherNodes != pi) {
                                            this.multiblocksWithChannel.add((GridNode) otherNodes);
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
            if (channelBottlenecks.getOrDefault(pi, 0) >= pi.getMaxChannels()) {
                return false;
            }

            pi = pi.getHighestSimilarParent();
        }

        // Allocate the channel along the path.
        pi = start;
        while (pi != null) {
            channelBottlenecks.addTo(pi, 1);
            pi = pi.getHighestSimilarParent();
        }

        channelNodes.add(start);
        return true;
    }

    private static final Object SUBTREE_END = new Object();

    /**
     * Propagates assignment to all nodes by performing a DFS. The implementation is iterative to avoid stack overflow.
     */
    private void propagateAssignments() {
        visited.clear();
        List<Object> stack = new ArrayList<>();

        for (var node : grid.getMachineNodes(ControllerBlockEntity.class)) {
            visited.add((IPathItem) node);
            for (var gcc : node.getConnections()) {
                var gc = (GridConnection) gcc;
                if (!(gc.getOtherSide(node).getOwner() instanceof ControllerBlockEntity)) {
                    visited.add(gc);
                    stack.add(gc);
                }
            }
        }

        while (!stack.isEmpty()) {
            Object current = stack.getLast();
            if (current == SUBTREE_END) {
                stack.removeLast();
                IPathItem item = (IPathItem) stack.removeLast();
                // We have visited the entire subtree and can now propagate channels upwards.
                if (item instanceof GridNode node) {
                    boolean hasChannel = channelNodes.contains(item);
                    channelsByBlocks += node.propagateChannelsUpwards(hasChannel);
                    if (hasChannel) {
                        channelsInUse++;
                    }
                } else {
                    channelsByBlocks += ((GridConnection) item).propagateChannelsUpwards();
                }
            } else {
                stack.add(SUBTREE_END);
                for (var pi : ((IPathItem) current).getPossibleOptions()) {
                    if (visited.add(pi)) {
                        stack.add(pi);
                    }
                }
            }
        }

        // Give a channel to all nodes that are a part of a multiblock that was given a channel before.
        for (var multiblockNode : multiblocksWithChannel) {
            multiblockNode.incrementChannelCount(1);
        }
    }

    public int getChannelsInUse() {
        return channelsInUse;
    }

    public int getChannelsByBlocks() {
        return channelsByBlocks;
    }
}
