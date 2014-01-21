package appeng.me.cache.helpers;

import appeng.api.networking.IGridConnection;

public class ConnectionWrapper
{

	public IGridConnection connection;

	public ConnectionWrapper(IGridConnection gc) {
		connection = gc;
	}

}