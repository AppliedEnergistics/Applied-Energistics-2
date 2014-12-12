package appeng.api.networking.spatial;

import appeng.api.networking.IGridCache;
import appeng.api.util.DimensionalCoord;

public interface ISpatialCache extends IGridCache
{

	/**
	 * @return true if a region is defined at all, it doesn't have to be valid, but all points must be in the same world.
	 */
	boolean hasRegion();

	/**
	 * @return true if the region defined is valid according to all rules.
	 */
	boolean isValidRegion();

	/**
	 * @return the minimum anchor point for the spatial region.
	 */
	DimensionalCoord getMin();

	/**
	 * @return the maximum anchor point for the spatial region.
	 */
	DimensionalCoord getMax();

	/**
	 * @return how many AE units are required to preform the activation
	 */
	long requiredPower();

	/**
	 * @return current 100% - 0% efficiency.
	 */
	float currentEfficiency();

}
