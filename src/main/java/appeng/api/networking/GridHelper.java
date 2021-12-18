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

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;

import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.events.GridEvent;
import appeng.me.GridConnection;
import appeng.me.GridEventBus;
import appeng.me.ManagedGridNode;

/**
 * A helper responsible for creating new {@link IGridNode}, and connecting existing nodes.
 *
 * @author yueh
 * @version rv5
 * @since rv5
 */
public final class GridHelper {
    private GridHelper() {
    }

    /**
     * Listens to events that are emitted per {@link IGrid}.
     */
    public static <T extends GridEvent> void addEventHandler(Class<T> eventClass, BiConsumer<IGrid, T> handler) {
        GridEventBus.subscribe(eventClass, handler);
    }

    /**
     * Forwards grid-wide events to the {@link IGridService} attached to that particular {@link IGrid}.
     */
    public static <T extends GridEvent, C extends IGridService> void addGridServiceEventHandler(Class<T> eventClass,
            Class<C> cacheClass,
            BiConsumer<C, T> eventHandler) {
        addEventHandler(eventClass, (grid, event) -> {
            eventHandler.accept(grid.getService(cacheClass), event);
        });
    }

    /**
     * Forwards grid-wide events to any node owner of a given type currently connected to that particular {@link IGrid}.
     *
     * @param nodeOwnerClass The class of node owner to forward the event to. Please note that subclasses are not
     *                       included.
     */
    public static <T extends GridEvent, C> void addNodeOwnerEventHandler(Class<T> eventClass,
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
    public static <T extends GridEvent, C> void addNodeOwnerEventHandler(Class<T> eventClass,
            Class<C> nodeOwnerClass,
            Consumer<C> eventHandler) {
        addEventHandler(eventClass, (grid, event) -> {
            for (C machine : grid.getMachines(nodeOwnerClass)) {
                eventHandler.accept(machine);
            }
        });
    }

    /**
     * Finds an {@link IInWorldGridNodeHost} at the given world location, or returns null if there isn't one.
     */
    @Nullable
    public static IInWorldGridNodeHost getNodeHost(LevelAccessor level, BlockPos pos) {
        if (level.hasChunkAt(pos)) {
            var te = level.getBlockEntity(pos);
            if (te instanceof IInWorldGridNodeHost host) {
                return host;
            }
        }
        return null;
    }

    /**
     * Given a known {@link IInWorldGridNodeHost}, find an adjacent grid node (i.e. for the purposes of making
     * connections) on another host in the world.
     * <p/>
     * Nodes that have been destroyed or have not completed initialization will not be returned.
     *
     * @see #getNodeHost(LevelAccessor, BlockPos)
     */
    @Nullable
    public static IGridNode getExposedNode(@Nonnull LevelAccessor level, @Nonnull BlockPos pos,
            @Nonnull Direction side) {
        var host = getNodeHost(level, pos);
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
     * Creates a managed grid node that makes managing the lifecycle of an {@link IGridNode} easier.
     * <p/>
     * This method can be called on both server and client.
     *
     * @param owner    The game object that owns the node, such as a block entity or {@link appeng.api.parts.IPart}.
     * @param listener A listener that will adapt events sent by the grid node to the owner.
     * @param <T>      The type of the owner.
     * @return The managed grid node.
     */
    @Nonnull
    public static <T> IManagedGridNode createManagedNode(@Nonnull T owner, @Nonnull IGridNodeListener<T> listener) {
        return new ManagedGridNode(owner, listener);
    }

    /**
     * Create a direct connection between two {@link IGridNode}.
     * <p>
     * This will be considered as having a distance of 1, regardless of the location of both nodes.
     *
     * @param a to be connected gridnode
     * @param b to be connected gridnode
     */
    public static IGridConnection createGridConnection(IGridNode a, IGridNode b) throws FailedConnectionException {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);

        return GridConnection.create(a, b, null);
    }

}
