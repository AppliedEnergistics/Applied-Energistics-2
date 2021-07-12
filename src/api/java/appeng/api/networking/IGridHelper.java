/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 AlgorithmX2
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

import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.events.GridEvent;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A helper responsible for creating new {@link IGridNode}, {@link IGridConnection} or potentially similar tasks.
 *
 * @author yueh
 * @version rv5
 * @since rv5
 */
public interface IGridHelper {
    /**
     * Listens to events that are emitted per {@link IGrid}.
     */
    <T extends GridEvent> void addEventHandler(Class<T> eventClass, BiConsumer<IGrid, T> handler);

    /**
     * Forwards grid-wide events to the {@link IGridCache} attached to that particular {@link IGrid}.
     */
    default <T extends GridEvent, C extends IGridCache> void addGridCacheEventHandler(Class<T> eventClass,
                                                                                      Class<C> cacheClass,
                                                                                      BiConsumer<C, T> eventHandler) {
        addEventHandler(eventClass, (grid, event) -> {
            eventHandler.accept(grid.getCache(cacheClass), event);
        });
    }

    /**
     * Forwards grid-wide events to any node owner of a given type currently connected to that particular {@link IGrid}.
     *
     * @param nodeOwnerClass The class of node owner to forward the event to. Please note that subclasses are not
     *                       included.
     */
    default <T extends GridEvent, C> void addNodeOwnerEventHandler(Class<T> eventClass,
                                                                   Class<C> nodeOwnerClass,
                                                                   BiConsumer<C, T> eventHandler) {
        addEventHandler(eventClass, (grid, event) -> {
            for (C machine : grid.getMachines(nodeOwnerClass)) {
                eventHandler.accept(machine, event);
            }
        });
    }

    /**
     * Convenience variant of {@link #addNodeOwnerEventHandler(Class, Class, BiConsumer)} where the event handler
     * doesn't care about the actual event object.
     */
    default <T extends GridEvent, C> void addNodeOwnerEventHandler(Class<T> eventClass,
                                                                   Class<C> nodeOwnerClass,
                                                                   Consumer<C> eventHandler) {
        addEventHandler(eventClass, (grid, event) -> {
            for (C machine : grid.getMachines(nodeOwnerClass)) {
                eventHandler.accept(machine);
            }
        });
    }

    /**
     * Finds a {@link IGridNodeHost} at the given world location, or returns null if there isn't one.
     */
    @Nullable
    IInWorldGridNodeHost getNodeHost(IWorld world, BlockPos pos);

    /**
     * Given a known {@link IInWorldGridNodeHost}, find an adjacent grid node (i.e. for the purposes of making connections)
     * on another host in the world.
     *
     * @see #getNodeHost(IWorld, BlockPos)
     */
    @Nullable
    default IGridNode getExposedNode(@Nonnull IWorld world, @Nonnull BlockPos pos, @Nonnull Direction side) {
        var host = getNodeHost(world, pos);
        if (host == null) {
            return null;
        }

        var node = host.getGridNode(side);
        if (node == null || !node.isExposedOnSide(side)) {
            return null;
        }

        return node;
    }

    /**
     * Create an in-world grid node for your {@link IGridNodeHost}
     */
    @Nonnull
    <T> IConfigurableGridNode createInWorldGridNode(@Nonnull T logicalHost,
                                                    @Nonnull IGridNodeListener<T> listener,
                                                    @Nonnull ServerWorld world,
                                                    @Nonnull BlockPos pos,
                                                    @Nonnull Set<GridFlags> flags);

    /**
     * Create an internal grid node that isn't accessible to the world, and won't automatically connect to other
     * in-world nodes.
     */
    @Nonnull
    <T> IConfigurableGridNode createInternalGridNode(@Nonnull T logicalHost,
                                                     @Nonnull IGridNodeListener<T> listener,
                                                     @Nonnull ServerWorld world,
                                                     @Nonnull Set<GridFlags> flags);

    /**
     * Create a direct connection between two {@link IGridNode}.
     * <p>
     * This will be considered as having a distance of 1, regardless of the location of both nodes.
     *
     * @param a to be connected gridnode
     * @param b to be connected gridnode
     */
    @Nonnull
    IGridConnection createGridConnection(@Nonnull IGridNode a, @Nonnull IGridNode b) throws FailedConnectionException;

}
