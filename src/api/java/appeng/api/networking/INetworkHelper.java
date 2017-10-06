
package appeng.api.networking;


import appeng.api.exceptions.FailedConnection;


public interface INetworkHelper
{

	/**
	 * create a grid node for your {@link appeng.api.networking.IGridHost}
	 *
	 * @param block grid block
	 *
	 * @return grid node of block
	 */
	IGridNode createGridNode( IGridBlock block );

	/**
	 * create a connection between two {@link appeng.api.networking.IGridNode}
	 *
	 * @param a to be connected gridnode
	 * @param b to be connected gridnode
	 *
	 * @throws appeng.api.exceptions.FailedConnection
	 */
	IGridConnection createGridConnection( IGridNode a, IGridNode b ) throws FailedConnection;

}