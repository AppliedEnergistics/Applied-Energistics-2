package appeng.api.storage;

import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

public interface IMEMonitor<T extends IAEStack> extends IMEInventoryHandler<T>, IBaseMonitor<T>
{

	@Override
	@Deprecated
	/**
	 * This method is discouraged when accessing data via a IMEMonitor
	 */
	public IItemList<T> getAvailableItems(IItemList out);

	/**
	 * Get access to the full item list of the network, preferred over {@link IMEInventory} .getAvailableItems(...)
	 * 
	 * @return full storage list.
	 */
	IItemList<T> getStorageList();

}
