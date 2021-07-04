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

import appeng.api.util.DimensionalBlockPos;
import net.minecraft.tileentity.TileEntity;

import appeng.api.parts.IPart;
import net.minecraft.world.World;

/**
 * Implement to create a networked {@link TileEntity} or {@link IPart} must be implemented for a part, or tile entity to
 * become part of a grid.
 */
public interface IGridNodeHost {

    /**
     * break this host, its violating security rules, just break your block, or part.
     */
    void securityBreak();

    World getWorld();

    /**
     * Notifies the grid host that properties of a node have changed that need to be saved to disk.
     * Can be implemented on tile-entities by delegating to {@link TileEntity#markDirty()} for example.
     */
    void saveChanges();

    /**
     * Called by the {@link IGridNode} when the visible connections for the node have changed, useful for cable.
     */
    default void onInWorldConnectionChanged(IGridNode node) {
    }

    /**
     * Called by the {@link IGridNode} when the node's owner has changed. The node's state needs to be saved.
     */
    default void onOwnerChanged(IGridNode node) {
    }

    /**
     * called when the grid for the node has changed, the general grid state should not be trusted at this point.
     */
    default void onGridChanged(IGridNode node) {
        saveChanges();
    }

}
