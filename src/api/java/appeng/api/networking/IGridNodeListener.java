package appeng.api.networking;

import net.minecraft.tileentity.TileEntity;

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
     * Can be implemented on tile-entities by delegating to {@link TileEntity#markDirty()} for example.
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
     * Called when one of the node's state properties has changed. Any of those changes might have potentially
     * changed the {@link IGridNode#isActive() active-state} as well.
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
         * The grid that the node's attached to has either started or finished booting up.
         */
        GRID_BOOT
    }

}
