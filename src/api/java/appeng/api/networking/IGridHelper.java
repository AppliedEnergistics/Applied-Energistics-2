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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import appeng.api.exceptions.FailedConnectionException;
import appeng.api.util.AEPartLocation;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Set;

/**
 * A helper responsible for creating new {@link IGridNode}, {@link IGridConnection} or potentially similar tasks.
 *
 * @author yueh
 * @version rv5
 * @since rv5
 */
public interface IGridHelper {

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
    default IGridNode getAdjacentNode(@Nonnull IInWorldGridNodeHost host, @Nonnull Direction direction) {
        var location = host.getLocation();
        var world = location.getWorld();
        var adjacentHost = getNodeHost(world, location.offset(direction));
        return adjacentHost != null ? adjacentHost.getGridNode(direction.getOpposite()) : null;
    }

    /**
     * Create a grid node for your {@link IGridNodeHost}
     */
    @Nonnull
    IConfigurableGridNode createGridNode(@Nonnull IGridNodeHost host, @Nonnull Set<GridFlags> flags);

    /**
     * Create a direct connection between two {@link IGridNode}.
     *
     * This will be considered as having a distance of 1, regardless of the location of both nodes.
     *
     * @param a to be connected gridnode
     * @param b to be connected gridnode
     */
    @Nonnull
    IGridConnection createGridConnection(@Nonnull IGridNode a, @Nonnull IGridNode b) throws FailedConnectionException;

}
