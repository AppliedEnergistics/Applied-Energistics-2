package appeng.api.implementations.tiles;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Crank/Crankable API,
 * 
 * Tiles that Implement this can receive power, from the crank, and have the
 * crank placed on them.
 * 
 * Tiles that access other tiles that implement this method can act as Cranks.
 * 
 * This interface must be implemented by a tile entity.
 */
public interface ICrankable
{

	/**
	 * Test if the crank can turn, return false if there is no work to be done.
	 * 
	 * @return if crank should be allowed to turn.
	 */
	boolean canTurn();

	/**
	 * The crank has completed one turn.
	 */
	void applyTurn();

	/**
	 * @return true if the crank can attach on the given side.
	 */
	boolean canCrankAttach(ForgeDirection directionToCrank);

}
