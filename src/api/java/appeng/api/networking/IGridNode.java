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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import appeng.api.networking.events.GridEvent;
import appeng.api.networking.ticking.ITickManager;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.world.server.ServerWorld;

import appeng.api.IAppEngApi;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.util.AEColor;

/**
 * Gives you a view into a Nodes connections and information.
 * <p>
 * updateState, getGrid, destroy are required to implement a proper IGridHost.
 * <p>
 * Don't Implement; Acquire from {@link IAppEngApi}.createGridNode
 */
public interface IGridNode {

    /**
     * Tries to get a service that was attached to this grid node when it was created. Used by overlay grids such as the
     * {@link appeng.api.networking.crafting.ICraftingGrid}.
     */
    @Nullable
    <T extends IGridNodeService> T getService(Class<T> serviceClass);

    /**
     * Gets the host of the grid node, which does not necessarily have a representation in the game world. In most
     * cases, this will be the game object that has created the node, for example a
     * {@link net.minecraft.tileentity.TileEntity} or {@link appeng.api.parts.IPart}, but may also represent something
     * entirely different.
     */
    @Nonnull
    Object getNodeOwner();

    /**
     * lets you walk the grid stating at the current node using a IGridVisitor, generally not needed, please use only if
     * required.
     *
     * @param visitor visitor
     */
    void beginVisit(@Nonnull IGridVisitor visitor);

    /**
     * get the grid for the node, this can change at a moments notice.
     *
     * @return grid or null if the node is still initializing
     */
    @Nullable
    IGrid getGrid();

    /**
     * @return True if the node has initialized and a grid is available.
     */
    boolean isReady();

    /**
     * @return the world the node is located in
     */
    @Nonnull
    ServerWorld getWorld();

    /**
     * @return The externally accessible sides of the host that this grid node has formed a connection through.
     */
    @Nonnull
    Set<Direction> getConnectedSides();

    /**
     * lets you iterate a nodes connections that have been made via the grid host's exposed sides to other adjacent grid
     * nodes.
     */
    @Nonnull
    Map<Direction, IGridConnection> getInWorldConnections();

    /**
     * lets you iterate all of a nodes connections that have been made either internally within the grid host, or to
     * other grid hosts, including connections made through the hosts sides and indirectly (QNB, tunnels). Includes
     * connections from {@link #getInWorldConnections()}.
     */
    @Nonnull
    List<IGridConnection> getConnections();

    /**
     * Reflects the networks status, returns true only if the network is powered, and the network has fully booted, and
     * this node has the channels it needs (if any).
     */
    default boolean isActive() {
        return isPowered() && hasGridBooted() && meetsChannelRequirements();
    }

    /**
     * @return True if the grid is connected to a network, and that network has fully booted.
     * @see IPathingGrid#isNetworkBooting()
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
    boolean hasFlag(@Nonnull GridFlags flag);

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
    boolean isExposedOnSide(@Nonnull Direction side);

    /**
     * @return An itemstack that will only be used to represent this grid node in user interfaces. Can return an
     *         {@link ItemStack#isEmpty() empty stack} to indicate the node should not be shown in the UI.
     */
    @Nonnull
    ItemStack getVisualRepresentation();

    /**
     * Colors can be used to prevent adjacent grid nodes from connecting. {@link AEColor#TRANSPARENT} indicates that the
     * node will connect to nodes of any color.
     */
    @Nonnull
    AEColor getGridColor();

    /**
     * Post an event to the grid this node is connected to.
     * Does nothing if the node isn't connected to any grid.
     */
    default <T extends GridEvent> T postEvent(T e) {
        var grid = getGrid();
        if (grid != null) {
            grid.postEvent(e);
        }
        return e;
    }

    /**
     * @return An {@link ITickManager} for managing the ticking behavior of machines connected to this grid.
     * If the grid node is not connected to a grid, a noop implementation will be returned.
     */
    default ITickManager getTickService() {
        var grid = getGrid();
        return grid != null ? grid.getService(ITickManager.class) : ITickManager.NOOP;
    }

}
