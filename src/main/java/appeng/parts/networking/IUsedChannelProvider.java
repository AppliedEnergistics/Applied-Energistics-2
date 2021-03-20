package appeng.parts.networking;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPart;

/**
 * Extended part that provides info about channel capacity and usage to probes like HWYLA and TheOneProbe.
 */
public interface IUsedChannelProvider extends IPart {

    /**
     * @return The number of channels carried on this cable. Purely for informational purposes.
     */
    default int getUsedChannelsInfo() {
        int howMany = 0;
        IGridNode node = this.getGridNode();
        if (node != null && node.isActive()) {
            for (final IGridConnection gc : node.getConnections()) {
                howMany = Math.max(gc.getUsedChannels(), howMany);
            }
        }
        return howMany;
    }

    /**
     * @return The number of channels that can be carried at most. Purely for informational purposes.
     */
    default int getMaxChannelsInfo() {
        IGridNode node = this.getGridNode();
        if (node != null) {
            IGridBlock gridBlock = node.getGridBlock();
            return gridBlock.getFlags().contains(GridFlags.DENSE_CAPACITY) ? 32 : 8;
        }
        return 0;
    }

}
