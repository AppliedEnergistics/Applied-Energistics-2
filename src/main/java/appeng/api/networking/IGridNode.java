/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.networking;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

import net.minecraft.CrashReportCategory;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.pathing.IPathingService;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AEColor;

/**
 * Gives you a view into a Nodes connections and information.
 * <p>
 * updateState, getGrid, destroy are required to implement a proper IGridHost.
 * <p>
 * Don't Implement; Acquire from {@link GridHelper} via {@link IManagedGridNode}.
 */
public interface IGridNode {

    /**
     * Tries to get a service that was attached to this grid node when it was created. Used by overlay grids such as the
     * {@link ICraftingService}.
     */
    @Nullable
    <T extends IGridNodeService> T getService(Class<T> serviceClass);

    /**
     * Gets the host of the grid node, which does not necessarily have a representation in the game level. In most
     * cases, this will be the game object that has created the node, for example a {@link BlockEntity} or
     * {@link appeng.api.parts.IPart}, but may also represent something entirely different.
     */

    Object getOwner();

    /**
     * lets you walk the grid stating at the current node using a IGridVisitor, generally not needed, please use only if
     * required.
     *
     * @param visitor visitor
     */
    void beginVisit(IGridVisitor visitor);

    /**
     * get the grid for the node, this can change at a moments notice.
     *
     * @return The grid this node is a part of.
     * @throws IllegalStateException If the node is being used after being destroyed or before it's initialized.
     */
    IGrid getGrid();

    /**
     * @return the level the node is located in
     */

    ServerLevel getLevel();

    /**
     * @return The externally accessible sides of the host that this grid node has formed a connection through.
     */

    Set<Direction> getConnectedSides();

    /**
     * lets you iterate a nodes connections that have been made via the grid host's exposed sides to other adjacent grid
     * nodes.
     */

    Map<Direction, IGridConnection> getInWorldConnections();

    /**
     * lets you iterate all of a nodes connections that have been made either internally within the grid host, or to
     * other grid hosts, including connections made through the hosts sides and indirectly (QNB, tunnels). Includes
     * connections from {@link #getInWorldConnections()}.
     */

    List<IGridConnection> getConnections();

    /**
     * Reflects the networks status, returns true only if the network is powered, and the network has fully booted, and
     * this node has the channels it needs (if any).
     * <p>
     * This should be used for active behavior such as network I/O, but {@link #isPassive()} should be used instead for
     * visual state display to avoid the device looking disabled while the grid is booting.
     */
    default boolean isActive() {
        return isPowered() && hasGridBooted() && meetsChannelRequirements();
    }

    /**
     * Return true only if the network is powered and the node has the channels it needs (if any).
     * <p>
     * This ignores whether the network is booting, so it should be used for "enabled" visuals or other "passive"
     * behavior, but should not perform active actions (such as network I/O) without checking that booting is finished.
     */
    default boolean isPassive() {
        return isPowered() && meetsChannelRequirements();
    }

    /**
     * @return True if the grid is connected to a network, and that network has fully booted.
     * @see IPathingService#isNetworkBooting()
     */
    boolean hasGridBooted();

    /**
     * @return True if the node has power from it's connected grid. Can be used to show a machine being powered, even if
     *         the machine doesn't have it's required channel or the network is still booting.
     * @see #isActive()
     */
    boolean isPowered();

    /**
     * @return if the node's channel requirements are currently met, use this for display purposes, use isActive for
     *         status.
     */
    boolean meetsChannelRequirements();

    /**
     * see if this node has a certain flag
     *
     * @param flag flags
     * @return true if has flag
     */
    boolean hasFlag(GridFlags flag);

    /**
     * @return the ownerID this represents the person who placed the node.
     * @see appeng.api.features.IPlayerRegistry
     */
    int getOwningPlayerId();

    /**
     * @return The power in AE/t that will be drained by this node.
     */
    @Nonnegative
    double getIdlePowerUsage();

    /**
     * @return True if the grid node is accessible on the given side of the host.
     */
    boolean isExposedOnSide(Direction side);

    /**
     * @return An item that will only be used to represent this grid node in user interfaces. Can return an
     *         <code>null</code> to indicate the node should not be shown in the UI.
     */
    @Nullable
    AEItemKey getVisualRepresentation();

    /**
     * Colors can be used to prevent adjacent grid nodes from connecting. {@link AEColor#TRANSPARENT} indicates that the
     * node will connect to nodes of any color.
     */

    AEColor getGridColor();

    /**
     * Fills in details about this node in the given crash report category.
     */
    void fillCrashReportCategory(CrashReportCategory category);

    int getMaxChannels();

    int getUsedChannels();
}
