package appeng.api.implementations.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import appeng.api.implementations.TransitionResult;
import appeng.api.util.WorldCoord;

/**
 * Implemented on a {@link Item}
 */
public interface ISpatialStorageCell
{

	/**
	 * @param is spatial storage cell
	 * @return true if this item is a spatial storage cell
	 */
	boolean isSpatialStorage(ItemStack is);

	/**
	 * @param is spatial storage cell
	 * @return the maximum size of the spatial storage cell along any given axis
	 */
	int getMaxStoredDim(ItemStack is);

	/**
	 * @param is spatial storage cell
	 * @return the world for this cell
	 */
	World getWorld(ItemStack is);

	/**
	 * get the currently stored size.
	 * 
	 * @param is spatial storage cell
	 * @return size of spatial
	 */
	WorldCoord getStoredSize(ItemStack is);

	/**
	 * Minimum coordinates in its world for the storage cell.
	 * 
	 * @param is spatial storage cell
	 * @return minimum coordinate of dimension
	 */
	WorldCoord getMin(ItemStack is);

	/**
	 * Maximum coordinates in its world for the storage cell.
	 * 
	 * @param is spatial storage cell
	 * @return maximum coordinate of dimension
	 */
	WorldCoord getMax(ItemStack is);

	/**
	 * Perform a spatial swap with the contents of the cell, and the world.
	 * 
	 * @param is spatial storage cell
	 * @param w world of spatial
	 * @param min min coord
	 * @param max max coord
	 * @param doTransition transition
	 * @return result of transition
	 */
	TransitionResult doSpatialTransition(ItemStack is, World w, WorldCoord min, WorldCoord max, boolean doTransition);

}