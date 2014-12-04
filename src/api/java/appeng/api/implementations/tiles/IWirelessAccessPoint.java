package appeng.api.implementations.tiles;

import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.DimensionalCoord;

public interface IWirelessAccessPoint extends IActionHost
{

	/**
	 * @return location of WAP
	 */
	DimensionalCoord getLocation();

	/**
	 * @return max range for this WAP
	 */
	double getRange();

	/**
	 * @return can you use this WAP?
	 */
	boolean isActive();

	/**
	 * @return grid of linked WAP
	 */
	IGrid getGrid();

}
