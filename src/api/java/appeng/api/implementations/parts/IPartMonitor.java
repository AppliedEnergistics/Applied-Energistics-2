package appeng.api.implementations.parts;

import appeng.api.networking.IGridHost;
import appeng.api.parts.IPart;

/**
 * Implemented by all screen like parts provided by AE.
 */
public interface IPartMonitor extends IPart, IGridHost
{

	/**
	 * @return if the device is online you should check this before providing
	 *         any other information.
	 */
	boolean isPowered();

}
