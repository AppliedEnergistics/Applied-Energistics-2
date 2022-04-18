/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
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

import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Interface that allows a grid node to notify it's host about various events.
 */
public interface IGridNodeListener<T> {
    /**
     * Called by the {@link IGridNode} to notify the host that the node is violating security rules and it needs to be
     * removed. Break or remove the host and drop it.
     */
    void onSecurityBreak(T nodeOwner, IGridNode node);

    /**
     * Called by the {@link IGridNode} when it's persistent state has changed and the host needs to ensure it is saved.
     * Can be implemented on block entities by delegating to {@link BlockEntity#setChanged()} for example.
     */
    void onSaveChanges(T nodeOwner, IGridNode node);

    /**
     * Called by the {@link IGridNode} when the visible connections for the node have changed, useful for cable.
     */
    default void onInWorldConnectionChanged(T nodeOwner, IGridNode node) {
    }

    /**
     * Called by the {@link IGridNode} when the node's owner has changed. The node's state needs to be saved.
     */
    default void onOwnerChanged(T nodeOwner, IGridNode node) {
        onSaveChanges(nodeOwner, node);
    }

    /**
     * called when the grid for the node has changed, the general grid state should not be trusted at this point.
     */
    default void onGridChanged(T nodeOwner, IGridNode node) {
    }

    /**
     * Called when one of the node's state properties has changed. Any of those changes might have potentially changed
     * the {@link IGridNode#isActive() active-state} as well.
     * <p>
     * Note that no update will be sent when the grid starts booting in spite of the active state changing. This is to
     * prevent parts turning off and back on for a split second. So make sure to always check the GridNode directly for
     * activeness before perform an action that requires it! An update will always be sent once booting is complete.
     *
     * @param state Indicates the node property that might have changed.
     */
    default void onStateChanged(T nodeOwner, IGridNode node, State state) {
    }

    /**
     * Gives a reason for why the active state of the node might have changed.
     */
    enum State {
        /**
         * The node's power status has changed (it either became powered or unpowered).
         */
        POWER,
        /**
         * The node's assigned channels have changed. This might only be relevant for nodes that require channels.
         */
        CHANNEL,
        /**
         * The grid that the node's attached to has just finished booting up.
         */
        GRID_BOOT
    }

}
