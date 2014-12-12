package appeng.api.storage;

import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;

/**
 * represents the internal behavior of a {@link ITileStorageMonitorable} use it to get this value for a tile, or part.
 * 
 * never check a tile for this, always go though ITileStorageMonitorable if you wish to use this interface.
 */
public interface IStorageMonitorable
{

	/**
	 * Access the item inventory for the monitorable storage.
	 */
	IMEMonitor<IAEItemStack> getItemInventory();

	/**
	 * Access the fluid inventory for the monitorable storage.
	 */
	IMEMonitor<IAEFluidStack> getFluidInventory();

}
