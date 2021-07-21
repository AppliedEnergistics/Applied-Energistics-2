package appeng.me.helpers;

import java.util.function.Consumer;

import net.minecraft.entity.player.PlayerEntity;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionHost;
import appeng.block.IOwnerAwareTile;
import appeng.me.ManagedGridNode;
import appeng.tile.AEBaseTileEntity;

/**
 * Interface implemented by the various AE2 tile entities that connect to the grid, to support callbacks from the tile
 * entities main grid node.
 */
public interface IGridConnectedTileEntity extends IActionHost, IOwnerAwareTile {

    /**
     * @return The main node that the tile entity uses to connect to the grid.
     */
    ManagedGridNode getMainNode();

    /**
     * @see ManagedGridNode#ifGridPresent(Consumer)
     */
    default boolean ifGridPresent(Consumer<IGrid> action) {
        return getMainNode().ifGridPresent(action);
    }

    /**
     * Used to break the tile when the grid detects a security violation. Implemented in
     * {@link AEBaseTileEntity#securityBreak()}
     */
    void securityBreak();

    /**
     * Used to save changes in the grid nodes contained in the tile entity to disk. Implemented in
     * {@link AEBaseTileEntity#saveChanges()}
     */
    void saveChanges();

    /**
     * Called when the tile entities main grid nodes power or channel assignment state changes. Primarily used to send
     * rendering updates to the client.
     */
    default void onMainNodeStateChanged(IGridNodeListener.State reason) {
    }

    @Override
    default IGridNode getActionableNode() {
        return getMainNode().getNode();
    }

    @Override
    default void setOwner(PlayerEntity owner) {
        getMainNode().setOwner(owner);
    }

}
