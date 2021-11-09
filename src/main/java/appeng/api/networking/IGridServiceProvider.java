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

import net.minecraft.world.level.Level;

/**
 * Allows you to create a grid-wide service. AE2 uses these for providing item, spatial, and tunnel services.
 * <p>
 * Any class that implements this needs to be registered with {@link GridServices}.
 */
public interface IGridServiceProvider {
    /**
     * Called each tick for the network, allows you to have active network wide behaviors.
     * <p>
     * Called at the beginning of a server tick.
     */
    default void onServerStartTick() {
    }

    /**
     * Called each tick for the network, allows you to have active network wide behaviors.
     * <p>
     * Called at the beginning of a level tick. Will happen for each {@link Level} separately.
     */
    default void onLevelStartTick(Level level) {
    }

    /**
     * Called each tick for the network, allows you to have active network wide behaviors.
     * <p>
     * Called at the end of a level tick. Will happen for each {@link Level} separately.
     */
    default void onLevelEndTick(Level level) {
    }

    /**
     * Called each tick for the network, allows you to have active network wide behaviors.
     * <p>
     * Called at the end of a server tick.
     */
    default void onServerEndTick() {
    }

    /**
     * Informs the grid service, that a node was removed from the grid.
     * <p>
     * Important: Do not trust the grids state in this method, interact only with the node you are passed, if you need
     * to manage other grid information, do it on the next updateTick.
     *
     * @param gridNode removed from that grid
     */
    default void removeNode(IGridNode gridNode) {
    }

    /**
     * Informs the grid service about a node that was added to the grid.
     * <p>
     * Important: Do not trust the grids state in this method, interact only with the node you are passed, if you need
     * to manage other grid information, do it on the next updateTick.
     *
     * @param gridNode added to grid node
     */
    default void addNode(IGridNode gridNode) {
    }

    /**
     * Called when a grid splits into two grids, AE will call a split as it iteratively processes changes. The
     * destination should receive half, and the current service should retain half.
     *
     * @param destinationStorage storage which receives half of old grid
     */
    default void onSplit(IGridStorage destinationStorage) {
    }

    /**
     * Called when two grids merge into one, AE will call a join as it Iteratively processes changes. Use this method to
     * incorporate all the data from the source into your service.
     *
     * @param sourceStorage old storage
     */
    default void onJoin(IGridStorage sourceStorage) {
    }

    /**
     * Called when saving changes,
     *
     * @param destinationStorage storage
     */
    default void populateGridStorage(IGridStorage destinationStorage) {
    }
}
