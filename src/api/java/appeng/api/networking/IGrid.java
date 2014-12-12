package appeng.api.networking;

import appeng.api.networking.events.MENetworkEvent;
import appeng.api.util.IReadOnlyCollection;

/**
 * Gives you access to Grid based information.
 * 
 * Don't Implement.
 */
public interface IGrid
{

	/**
	 * Get Access to various grid modules
	 * 
	 * @param iface face
	 * @return the IGridCache you requested.
	 */
	public <C extends IGridCache> C getCache(Class<? extends IGridCache> iface);

	/**
	 * Post an event into the network event bus.
	 * 
	 * @param ev
	 *            - event to post
	 * @return returns ev back to original poster
	 */
	public MENetworkEvent postEvent(MENetworkEvent ev);

	/**
	 * Post an event into the network event bus, but direct it at a single node.
	 * 
	 * @param ev
	 *            event to post
	 * @return returns ev back to original poster
	 */
	public MENetworkEvent postEventTo(IGridNode node, MENetworkEvent ev);

	/**
	 * get a list of the diversity of classes, you can use this to better detect which machines your interested in,
	 * rather then iterating the entire grid to test them.
	 * 
	 * @return IReadOnlyCollection of all available host types (Of Type IGridHost).
	 */
	public IReadOnlyCollection<Class<? extends IGridHost>> getMachinesClasses();

	/**
	 * Get machines on the network.
	 * 
	 * @param gridHostClass class of the grid host
	 * @return IMachineSet of all nodes belonging to hosts of specified class.
	 */
	public IMachineSet getMachines(Class<? extends IGridHost> gridHostClass);

	/**
	 * @return IReadOnlyCollection for all nodes on the network, node visitors are preferred.
	 */
	IReadOnlyCollection<IGridNode> getNodes();

	/**
	 * @return true if the last node has been removed from the grid.
	 */
	public boolean isEmpty();

	/**
	 * @return the node considered the pivot point of the grid.
	 */
	public IGridNode getPivot();

}
