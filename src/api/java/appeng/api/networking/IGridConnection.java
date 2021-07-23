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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;

/**
 * Access to AE's internal grid connections.
 * <p>
 * Messing with connection is generally completely unnecessary, you should be able to just use IGridNode.updateState()
 * to have AE manage them for you.
 * <p>
 * Don't Implement.
 */
public interface IGridConnection {

    /**
     * lets you get the opposing node of the connection by passing your own node.
     *
     * @param gridNode current grid node
     * @return the IGridNode which represents the opposite side of the connection.
     */
    @Nonnull
    IGridNode getOtherSide(IGridNode gridNode);

    /**
     * @return True if this connection was established via the grid node host's sides, and
     *         {@link #getDirection(IGridNode)} returns a non-null value.
     */
    boolean isInWorld();

    /**
     * determine the direction of the connection based on your node.
     *
     * @param sourceNode current grid node
     * @return the direction of the connection, if {@link #isInWorld()} is true, otherwise null.
     */
    @Nullable
    Direction getDirection(IGridNode sourceNode);

    /**
     * by destroying a connection you may create new grids, and trigger un-expected behavior, you should only destroy
     * connections if you created them.
     */
    void destroy();

    /**
     * @return node A
     */
    @Nonnull
    IGridNode a();

    /**
     * @return node B
     */
    @Nonnull
    IGridNode b();

    /**
     * @return how many channels pass over this connections.
     */
    int getUsedChannels();
}
