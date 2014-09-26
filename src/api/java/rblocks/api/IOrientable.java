package rblocks.api;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Implement on a tile entity to manage storage of rotation information.
 * 
 * remember for optional dependencies use:
 * 
 * @Interface(iface = "rblocks.api.IOrientable", modid = "RotateableBlocks")
 * 
 * this will strip the interface at runtime and allow your mod to work without RotatableBlocks.
 * 
 */
public interface IOrientable
{

	/**
	 * @return true or false, if the tile rotation is meaningful, or even changeable
	 */
	boolean canBeRotated();

	/**
	 * @return the direction the tile is facing
	 */
	ForgeDirection getForward();

	/**
	 * @return the direction top of the tile
	 */
	ForgeDirection getUp();

	/**
	 * Update the orientation
	 * 
	 * @param Forward
	 * @param Up
	 */
	void setOrientation(ForgeDirection Forward, ForgeDirection Up);

}