package appeng.api.networking.pathing;

import appeng.api.networking.IGridCache;

public interface IPathingGrid extends IGridCache
{

	/**
	 * @return true if the network is in its booting stage
	 */
	boolean isNetworkBooting();

	/**
	 * @return the controller state of the network, useful if you want to
	 *         require a controller for a feature.
	 */
	ControllerState getControllerState();

	/**
	 * trigger a network reset, booting, pathfinding and all.
	 */
	void repath();

}