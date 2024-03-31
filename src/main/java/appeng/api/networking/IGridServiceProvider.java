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

import java.io.IOException;

import com.google.gson.stream.JsonWriter;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
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

    @Deprecated(forRemoval = true, since = "1.20.1")
    default void addNode(IGridNode gridNode) {
    }

    /**
     * Informs the grid service about a node that was added to the grid.
     * <p>
     * Important: Do not trust the grids state in this method, interact only with the node you are passed, if you need
     * to manage other grid information, do it on the next updateTick.
     *
     * @param gridNode  added to grid node
     * @param savedData The grid-related saved data for the node joining the grid. May be null. Contains data written by
     *                  {@link #saveNodeData}.
     */
    default void addNode(IGridNode gridNode, @Nullable CompoundTag savedData) {
        addNode(gridNode);
    }

    /**
     * Save provider-specific data for the given node to the given tag. Note that the tag is shared between all
     * providers, so take care to use unique names for your properties!
     */
    default void saveNodeData(IGridNode gridNode, CompoundTag savedData) {
    }

    /**
     * Write debug information about this service to the given writer.
     */
    default void debugDump(JsonWriter writer) throws IOException {
    }
}
