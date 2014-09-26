package mekanism.api.gas;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Implement this if your block can connect to Pressurized Tubes.
 * @author AidanBrady
 *
 */
public interface ITubeConnection
{
	/**
	 * Whether or not a tube can connect to a certain orientation.
	 * @param side - orientation to check
	 * @return if a tube can connect
	 */
	public boolean canTubeConnect(ForgeDirection side);
}
