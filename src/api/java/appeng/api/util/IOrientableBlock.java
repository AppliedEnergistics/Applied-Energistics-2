package appeng.api.util;

import net.minecraft.world.IBlockAccess;

/**
 * Implemented on many of AE's non Tile Entity Blocks as a way to get a IOrientable.
 */
public interface IOrientableBlock
{

	/**
	 * @return if this block uses metadata to store its rotation.
	 */
	boolean usesMetadata();

	/**
	 * @param world world of block
	 * @param x x pos of block
	 * @param y y pos of block
	 * @param z z pos of block
	 * @return a IOrientable if applicable
	 */
	IOrientable getOrientable(IBlockAccess world, int x, int y, int z);

}