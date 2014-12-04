package appeng.api.networking.events;

import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;

/**
 * posted by the network when the networks Storage Changes, you can use the currentItems list to check levels, and
 * update status.
 * 
 * this is the least useful method of getting info about changes in the network.
 * 
 * Do not modify the list or its contents in anyway.
 */
public class MENetworkStorageEvent extends MENetworkEvent
{

	public final IMEMonitor monitor;
	public final StorageChannel channel;

	public MENetworkStorageEvent(IMEMonitor o, StorageChannel chan) {
		monitor = o;
		channel = chan;
	}

}
