package appeng.me.cache;

import java.util.LinkedList;

import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;

public class NetworkMonitor<T extends IAEStack<T>> extends MEMonitorHandler<T>
{

	final private GridStorageCache myGridCache;
	final private StorageChannel myChannel;

	boolean sendEvent = false;

	public NetworkMonitor(GridStorageCache cache, StorageChannel chan) {
		super( null );
		myGridCache = cache;
		myChannel = chan;
	}

	final static public LinkedList depth = new LinkedList();

	@Override
	protected void postChange(T diff)
	{
		if ( depth.contains( this ) )
			return;

		depth.push( this );

		sendEvent = true;
		super.postChange( diff );

		Object last = depth.pop();
		if ( last != this )
			throw new RuntimeException( "Invalid Access to Networked Storage API detected." );
	}

	public void onTick()
	{
		if ( sendEvent )
		{
			sendEvent = false;
			myGridCache.myGrid.postEvent( new MENetworkStorageEvent( getStorageList(), myChannel ) );
		}
	}

	@Override
	protected IMEInventoryHandler getHandler()
	{
		switch (myChannel)
		{
		case ITEMS:
			return myGridCache.getItemInventoryHandler();
		case FLUIDS:
			return myGridCache.getFluidInventoryHandler();
		default:
		}
		return null;
	}

}
