package appeng.api.networking;

import java.util.Iterator;

/**
 * An extension of IGridBlock, only means something when your getFlags() contains REQUIRE_CHANNEL, when done properly it
 * will call the method to get a list of all related nodes and give each of them a channel simultaneously for the entire
 * set. This means your entire Multiblock can work with a single channel, instead of one channel per block.
 */
public interface IGridMultiblock extends IGridBlock
{

	/**
	 * Used to acquire a list of all nodes that are part of the multiblock.
	 * 
	 * @return an iterator that will iterate all the nodes for the multiblock. ( read-only iterator expected. )
	 */
	Iterator<IGridNode> getMultiblockNodes();

}
