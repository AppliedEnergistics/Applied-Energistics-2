package appeng.me.helpers;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;

/**
 * A simple node listener for {@link IGridConnectedTileEntity} that host nodes and don't have special requirements.
 */
public class TileEntityNodeListener<T extends IGridConnectedTileEntity> implements IGridNodeListener<T> {
    public static final TileEntityNodeListener<IGridConnectedTileEntity> INSTANCE = new TileEntityNodeListener<>();

    @Override
    public void onSecurityBreak(T nodeOwner, IGridNode node) {
        nodeOwner.securityBreak();
    }

    @Override
    public void onSaveChanges(T nodeOwner, IGridNode node) {
        nodeOwner.saveChanges();
    }

    @Override
    public void onStateChanged(T nodeOwner, IGridNode node, State state) {
        nodeOwner.onMainNodeStateChanged(state);
    }

}
