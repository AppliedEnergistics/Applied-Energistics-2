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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.core.AEConfig;
import appeng.me.GridConnection;
import appeng.me.GridNode;
import appeng.me.pathfinding.ControllerInfo.SubtreeInfo;
import appeng.me.pathfinding.ControllerInfo.TrunkSearchState;

/**
 * Calculation to assign channels starting from the controllers. The full computation is split in two steps, each linear
 * time.
 * <p>
 * First, a BFS is performed starting from the controllers. This establishes a tree that connects all path items to a
 * controller. As nodes that require channels are visited, they are assigned a channel if possible. This is done by
 * checking the channel count of a few key nodes (max 3) along the path.
 * <p>
 * Second, a DFS is performed to propagate the channel count upwards.
 */
public class PathingCalculation {
    private static final Logger LOG = LoggerFactory.getLogger(PathingCalculation.class);

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
    /** True if multi-controller is enabled and controllers are connected wrongly. */
    private boolean invalidTrunkConnection = false;
    /** Whether multi-controller mode is enabled */
    private final boolean multiController = AEConfig.instance().allowMultiController();
    /**
     * Tracks SubtreeInfo objects for multi-controller routing. Null unless multi-controller is enabled
     */
    @Nullable
    private final Map<GridConnection, SubtreeInfo> subtreeInfoMap;
    /**
     * Tracks ControllerInfo objects for multi-controller routing. Null unless multi-controller is enabled
     */
    @Nullable
    private final Map<IGridNode, ControllerInfo> controllerInfoMap;

    /**
     * Create a new pathing calculation from the passed grid.
     */
    public PathingCalculation(IGrid grid) {
        this.grid = grid;
        if (multiController) {
            subtreeInfoMap = new HashMap<>();
            controllerInfoMap = new HashMap<>();
        } else {
            subtreeInfoMap = null;
            controllerInfoMap = null;
        }

        // Add every outgoing connection of the controllers (that doesn't point to another controller) to the list.
        for (var node : grid.getMachineNodes(ControllerBlockEntity.class)) {
            visited.add((IPathItem) node);
            ControllerInfo controllerInfo;
            if (multiController) {
                controllerInfo = new ControllerInfo(this, node);
                controllerInfoMap.put(node, controllerInfo);
            } else {
                controllerInfo = null;
            }
            for (var gcc : node.getConnections()) {
                var gc = (GridConnection) gcc;
                if (!(gc.getOtherSide(node).getOwner() instanceof ControllerBlockEntity)) {
                    enqueue(gc, 0);
                    gc.setControllerRoute((GridNode) node);
                    if (multiController) {
                        var subtreeInfo = controllerInfo.forSubtree(gc);
                        subtreeInfoMap.put(gc, subtreeInfo);
                        gc.setSubtreeInfo(subtreeInfo);
                    }
                } else if (multiController) {
                    var other = getSubtreeInfo(gc);
                    if (other == null) {
                        var subtreeInfo = controllerInfo.forSubtree(gc);
                        subtreeInfoMap.put(gc, subtreeInfo);
                        gc.setSubtreeInfo(subtreeInfo);
                    } else {
                        // do the merge
                        ControllerInfo.mergeMembers(other.parent(), controllerInfo);
                    }
                }
            }
        }
    }

    private @Nullable SubtreeInfo getSubtreeInfo(IPathItem node) {
        // was this a mistake? perhaps!
        var ref = node.getSubtreeInfo();
        if (ref == null) {
            return null;
        }
        var nc = ref.get();
        if (nc == null) {
            return null;
        }
        // ensure correct generation
        if (nc.parent().owner != this) {
            return null;
        }
        return nc;
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

        if (multiController && controllerInfoMap != null) {
            var firstCluster = controllerInfoMap.values().iterator().next();
            if (firstCluster.members.size() != controllerInfoMap.size()) {
                invalidTrunkConnection = true;
                return;
            }
        }

        // DFS pass
        propagateAssignments();
    }

    private void processQueue(Queue<IPathItem> oldOpen, int queueIndex) {
        while (!oldOpen.isEmpty()) {
            IPathItem i = oldOpen.poll();
            for (IPathItem pi : i.getPossibleOptions()) {
                if (this.visited.contains(pi)) {
                    if (multiController && queueIndex == 0 && pi.getControllerRoute() != i) {
                        // check if this should be a trunk link
                        maybeDoTrunk(i, pi);
                    }
                    continue;
                }

                // Set BFS parent.
                pi.setControllerRoute(i);

                if (pi.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
                    if (!this.multiblocksWithChannel.contains(pi)) {
                        // Try to use the channel along the path.
                        boolean worked = tryUseChannel((GridNode) pi);

                        if (multiController && queueIndex == 0 && worked) {
                            // cannot trunk if any channel is allocated here
                            var subtreeInfo = getSubtreeInfo(i);
                            if (subtreeInfo != null) {
                                subtreeInfo.trunkState = TrunkSearchState.INVALID;
                            }
                        }

                        if (worked && pi.hasFlag(GridFlags.MULTIBLOCK)) {
                            var multiblock = ((IGridNode) pi).getService(IGridMultiblock.class);
                            if (multiblock != null) {
                                var oni = multiblock.getMultiblockNodes();
                                while (oni.hasNext()) {
                                    final IGridNode otherNodes = oni.next();
                                    if (otherNodes == null) {
                                        // Only a log for now until addons are fixed too. See
                                        // https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/8295
                                        LOG.error("Skipping null node returned by grid multiblock node {}",
                                                multiblock);
                                    } else if (otherNodes != pi) {
                                        this.multiblocksWithChannel.add((GridNode) otherNodes);
                                    }
                                }
                            }
                        }
                    }
                }

                // propagate nearestController (optimization: only check in dense queue)
                outer: if (multiController && queueIndex == 0) {
                    var parentSubtree = getSubtreeInfo(i);
                    if (parentSubtree == null) {
                        break outer;
                    }
                    if (parentSubtree.trunkState != TrunkSearchState.SEARCHING) {
                        // no reason to proceed further
                        break outer;
                    }
                    if (!(pi instanceof GridConnection) && !pi.hasFlag(GridFlags.DENSE_CAPACITY)) {
                        // not valid to propagate trunk
                        break outer;
                    }
                    if (pi.hasFlag(GridFlags.CANNOT_CARRY_COMPRESSED)) {
                        // disallow trunk connections over p2p tunnels
                        break outer;
                    }
                    pi.setSubtreeInfo(parentSubtree);
                }

                enqueue(pi, queueIndex);
            }
        }
    }

    private void maybeDoTrunk(IPathItem current, IPathItem other) {
        var currentSt = getSubtreeInfo(current);
        if (currentSt == null || currentSt.trunkState != TrunkSearchState.SEARCHING) {
            return;
        }
        var otherSt = getSubtreeInfo(other);
        if (otherSt == null || otherSt.trunkState != TrunkSearchState.SEARCHING) {
            return;
        }
        if (currentSt.parent().members == otherSt.parent().members) {
            return;
        }

        // we are ok to trunk
        var currentNc = currentSt.parent();
        var otherNc = otherSt.parent();
        ControllerInfo.mergeMembers(currentNc, otherNc);
        currentSt.trunkState = TrunkSearchState.CONNECTED;
        otherSt.trunkState = TrunkSearchState.CONNECTED;
        var maxChannels = grid.getPathingService().getChannelMode().getCableCapacityFactor() * 32;

        // trunkState will be INVALID if there are already any channels assigned on this subtree
        // so we are free to allocate channels here
        var sideA = current;
        while (sideA != currentNc.controllerNode) {
            if (sideA instanceof GridConnection gc) {
                gc.setAdHocChannels(maxChannels);
            } else if (sideA instanceof GridNode gn) {
                gn.incrementChannelCount(maxChannels);
            }
            sideA = sideA.getControllerRoute();
        }
        var sideB = other;
        while (sideB != otherNc.controllerNode) {
            if (sideB instanceof GridConnection gc) {
                gc.setAdHocChannels(maxChannels);
            } else if (sideB instanceof GridNode gn) {
                gn.incrementChannelCount(maxChannels);
            }
            sideB = sideB.getControllerRoute();
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
            var subtreeInfo = getSubtreeInfo(pi);
            if (subtreeInfo != null && subtreeInfo.trunkState == TrunkSearchState.CONNECTED) {
                // this subtree is currently used by a trunk connection
                return false;
            }

            pi = pi.getHighestSimilarAncestor();
        }

        // Allocate the channel along the path.
        pi = start;
        while (pi != null) {
            channelBottlenecks.addTo(pi, 1);
            pi = pi.getHighestSimilarAncestor();
        }

        channelNodes.add(start);
        return true;
    }

    private static final Object SUBTREE_END = new Object();

    /**
     * Propagates assignment to all nodes by performing a DFS. The implementation is iterative to avoid stack overflow.
     */
    private void propagateAssignments() {
        List<Object> stack = new ArrayList<>();
        Set<IPathItem> controllerNodes = new HashSet<>();

        for (var node : grid.getMachineNodes(ControllerBlockEntity.class)) {
            controllerNodes.add((IPathItem) node);
            for (var gcc : node.getConnections()) {
                var gc = (GridConnection) gcc;
                if (multiController && subtreeInfoMap != null) {
                    var subtreeInfo = subtreeInfoMap.get(gc);
                    if (subtreeInfo != null && subtreeInfo.trunkState == TrunkSearchState.CONNECTED) {
                        // ignore this subtree
                        continue;
                    }
                }
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
                    // The neighbor could either be: a child, the parent, or in a different tree if it is closer to
                    // another controller. It is a child if we are its parent.
                    // We need to exclude controller nodes because their getControllerRoute() is nonsense.
                    if (!controllerNodes.contains(pi) && pi.getControllerRoute() == current) {
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

    public boolean isInvalidTrunkConnection() {
        return invalidTrunkConnection;
    }
}
