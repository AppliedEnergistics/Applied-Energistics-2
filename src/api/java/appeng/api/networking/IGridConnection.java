package appeng.api.networking;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Access to AE's internal grid connections.
 * 
 * Messing with connection is generally completely unnecessary, you should be able to just use IGridNode.updateState()
 * to have AE manage them for you.
 * 
 * Don't Implement.
 */
public interface IGridConnection
{

	/**
	 * lets you get the opposing node of the connection by passing your own node.
	 * 
	 * @param gridNode current grid node
	 * @return the IGridNode which represents the opposite side of the connection.
	 */
	IGridNode getOtherSide(IGridNode gridNode);

	/**
	 * determine the direction of the connection based on your node.
	 * 
	 * @param gridNode current grid node
	 * @return the direction of the connection, only valid for in world connections.
	 */
	ForgeDirection getDirection(IGridNode gridNode);

	/**
	 * by destroying a connection you may create new grids, and trigger un-expected behavior, you should only destroy
	 * connections if you created them.
	 */
	void destroy();

	/**
	 * @return node A
	 */
	IGridNode a();

	/**
	 * @return node B
	 */
	IGridNode b();

	/**
	 * @return if the connection is invisible this returns false
	 */
	boolean hasDirection();

	/**
	 * @return how many channels pass over this connections.
	 */
	int getUsedChannels();

}