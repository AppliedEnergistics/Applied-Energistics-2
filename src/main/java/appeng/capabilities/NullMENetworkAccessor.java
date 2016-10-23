package appeng.capabilities;


import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;


class NullMENetworkAccessor implements IStorageMonitorableAccessor
{

	@Override
	public IStorageMonitorable getInventory( BaseActionSource src )
	{
		return null;
	}

}
