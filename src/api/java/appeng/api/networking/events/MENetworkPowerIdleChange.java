package appeng.api.networking.events;

import appeng.api.networking.IGridNode;

/**
 * Implementers of a IGridBlock must post this event when your getIdlePowerUsage
 * starts returning a new value, if you do not post this event the network will
 * not change the idle draw.
 * 
 * you do not need to send this event when your node is added / removed from the grid.
 */
public class MENetworkPowerIdleChange extends MENetworkEvent
{

	public final IGridNode node;

	public MENetworkPowerIdleChange(IGridNode nodeThatChanged) {
		node = nodeThatChanged;
	}

}
