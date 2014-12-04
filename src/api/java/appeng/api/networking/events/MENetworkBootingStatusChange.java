package appeng.api.networking.events;

import appeng.api.networking.IGridNode;

/**
 * Posted by the network when the booting status of the network goes up
 * or down, the change is reflected via {@link IGridNode}.isActive()
 * 
 * Note: Most machines just need to check {@link IGridNode}.isActive()
 */
public class MENetworkBootingStatusChange extends MENetworkEvent
{

}
