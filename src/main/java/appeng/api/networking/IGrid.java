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

import java.util.Set;

import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.events.GridEvent;
import appeng.api.networking.pathing.IPathingService;
import appeng.api.networking.security.ISecurityService;
import appeng.api.networking.spatial.ISpatialService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.networking.ticking.ITickManager;

/**
 * Gives you access to Grid based information.
 * <p>
 * Don't Implement.
 */
public interface IGrid {

    /**
     * Get Access to various grid modules
     *
     * @param iface face
     * @return the IGridCache you requested.
     */

    <C extends IGridService> C getService(Class<C> iface);

    /**
     * Post an event into the network event bus.
     *
     * @param ev - event to post
     * @return returns ev back to original poster
     */

    <T extends GridEvent> T postEvent(T ev);

    /**
     * get a list of the diversity of classes, you can use this to better detect which machines your interested in,
     * rather then iterating the entire grid to test them.
     *
     * @return IReadOnlyCollection of all available host types (Of Type IGridHost).
     */

    Iterable<Class<?>> getMachineClasses();

    /**
     * Get machine nodes on the network.
     *
     * @param machineClass class of the machine associated with a grid node
     * @return all nodes belonging to machines of specified class. keep in mind that machines can have multiple nodes.
     */

    Iterable<IGridNode> getMachineNodes(Class<?> machineClass);

    /**
     * Get machines connected to the network via grid nodes.
     *
     * @param machineClass class of the machine associated with a grid node
     * @return all unique machines of specified class. if a machine is connected to the grid with multiple nodes, this
     *         will only return the machine once.
     */

    <T> Set<T> getMachines(Class<T> machineClass);

    /**
     * Get machines connected to the network via grid nodes that are powered and have their needed channels.
     *
     * @param machineClass class of the machine associated with a grid node
     * @return all unique machines of specified class. if a machine is connected to the grid with multiple nodes, this
     *         will only return the machine once.
     */

    <T> Set<T> getActiveMachines(Class<T> machineClass);

    /**
     * @return IReadOnlyCollection for all nodes on the network, node visitors are preferred.
     */

    Iterable<IGridNode> getNodes();

    /**
     * @return true if the last node has been removed from the grid.
     */
    boolean isEmpty();

    /**
     * @return the node considered the pivot point of the grid.
     */

    IGridNode getPivot();

    /**
     * @return The number of nodes in this grid.
     */
    int size();

    /**
     * Get this grids {@link ITickManager}.
     *
     * @see #getService(Class)
     */

    default ITickManager getTickManager() {
        return getService(ITickManager.class);
    }

    /**
     * Get this grids {@link IStorageService}.
     *
     * @see #getService(Class)
     */

    default IStorageService getStorageService() {
        return getService(IStorageService.class);
    }

    /**
     * Get this grids {@link IEnergyService}.
     *
     * @see #getService(Class)
     */

    default IEnergyService getEnergyService() {
        return getService(IEnergyService.class);
    }

    /**
     * Get this grids {@link ICraftingService}.
     *
     * @see #getService(Class)
     */

    default ICraftingService getCraftingService() {
        return getService(ICraftingService.class);
    }

    /**
     * Get this grids {@link ISecurityService}.
     *
     * @see #getService(Class)
     */

    default ISecurityService getSecurityService() {
        return getService(ISecurityService.class);
    }

    /**
     * Get this grids {@link IPathingService}.
     *
     * @see #getService(Class)
     */

    default IPathingService getPathingService() {
        return getService(IPathingService.class);
    }

    /**
     * Get this grids {@link ISpatialService}.
     *
     * @see #getService(Class)
     */

    default ISpatialService getSpatialService() {
        return getService(ISpatialService.class);
    }
}
