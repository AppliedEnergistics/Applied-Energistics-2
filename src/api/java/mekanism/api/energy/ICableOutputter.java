package mekanism.api.energy;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Implement this if your TileEntity is capable of outputting energy to cables, overriding Mekanism's default implementation.
 * @author AidanBrady
 *
 */
public interface ICableOutputter
{
	/**
	 * Whether or not this block can output to a cable on a specific side.
	 * @param side - side to check
	 * @return if the block can output
	 */
	public boolean canOutputTo(ForgeDirection side);
}
