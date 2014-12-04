package appeng.api.networking;

/**
 * 
 * Allows you to create a network wise service, AE2 uses these for providing
 * item, spatial, and tunnel services.
 * 
 * Any Class that implements this, should have a public default constructor that
 * takes a single argument of type IGrid.
 * 
 */
public interface IGridCache
{

	/**
	 * Called each tick for the network, allows you to have active network wide
	 * behaviors.
	 * 
	 */
	void onUpdateTick();

	/**
	 * inform your cache, that a machine was removed from the grid.
	 * 
	 * Important: Do not trust the grids state in this method, interact only
	 * with the node you are passed, if you need to manage other grid
	 * information, do it on the next updateTick.
	 * 
	 * @param gridNode removed from that grid
	 * @param machine to be removed machine
	 */
	void removeNode(IGridNode gridNode, IGridHost machine);

	/**
	 * informs you cache that a machine was added to the grid.
	 * 
	 * Important: Do not trust the grids state in this method, interact only
	 * with the node you are passed, if you need to manage other grid
	 * information, do it on the next updateTick.
	 * 
	 * @param gridNode added to grid node
	 * @param machine to be added machine
	 */
	void addNode(IGridNode gridNode, IGridHost machine);

	/**
	 * Called when a grid splits into two grids, AE will call a split as it
	 * Iteratively processes changes. The destination should receive half, and
	 * the current cache should receive half.
	 * 
	 * @param destinationStorage storage which receives half of old grid
	 */
	void onSplit(IGridStorage destinationStorage);

	/**
	 * Called when two grids merge into one, AE will call a join as it
	 * Iteratively processes changes. Use this method to incorporate all the
	 * data from the source into your cache.
	 * 
	 * @param sourceStorage old storage
	 */
	void onJoin(IGridStorage sourceStorage);

	/**
	 * Called when saving changes,
	 * 
	 * @param destinationStorage storage
	 */
	void populateGridStorage(IGridStorage destinationStorage);

}
