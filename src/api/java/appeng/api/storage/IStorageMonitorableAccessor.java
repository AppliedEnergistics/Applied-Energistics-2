
package appeng.api.storage;


import javax.annotation.Nullable;

import appeng.api.networking.security.IActionSource;


/**
 * Allows storage buses to request access to another ME network so it can be used as a subnetwork.
 * This interface is used in conjunction with capabilities, so when an object of this is obtained,
 * it already knows about which face the access was requested from.
 * <p/>
 * To get access to the capability for this, use @CapabilityInject with this interface as the argument
 * to the annotation.
 */
public interface IStorageMonitorableAccessor
{

	/**
	 * @return Null if the network cannot be accessed by the given action source (i.e. security doesn't permit it).
	 */
	@Nullable
	IStorageMonitorable getInventory( IActionSource src );
}
