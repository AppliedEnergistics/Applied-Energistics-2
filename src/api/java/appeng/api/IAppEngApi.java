package appeng.api;

import appeng.api.definitions.Blocks;
import appeng.api.definitions.Items;
import appeng.api.definitions.Materials;
import appeng.api.definitions.Parts;
import appeng.api.exceptions.FailedConnection;
import appeng.api.features.IRegistryContainer;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartHelper;
import appeng.api.storage.IStorageHelper;

public interface IAppEngApi
{

	/**
	 * @return Registry Container for the numerous registries in AE2.
	 */
	IRegistryContainer registries();

	/**
	 * @return helper for working with storage data types.
	 */
	IStorageHelper storage();

	/**
	 * @return helper for working with grids, and buses.
	 */
	IPartHelper partHelper();

	/**
	 * @return an accessible list of all of AE's Items
	 */
	Items items();

	/**
	 * @return an accessible list of all of AE's materials; materials are items
	 */
	Materials materials();

	/**
	 * @return an accessible list of all of AE's blocks
	 */
	Blocks blocks();

	/**
	 * @return an accessible list of all of AE's parts, parts are items
	 */
	Parts parts();

	/**
	 * create a grid node for your {@link IGridHost}
	 * 
	 * @param block grid block
	 * @return grid node of block
	 */
	IGridNode createGridNode(IGridBlock block);

	/**
	 * create a connection between two {@link IGridNode}
	 * 
	 * @param a to be connected gridnode
	 * @param b to be connected gridnode
	 * @throws FailedConnection
	 */
	IGridConnection createGridConnection(IGridNode a, IGridNode b) throws FailedConnection;

}