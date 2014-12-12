package appeng.api.networking.storage;

import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEStack;

public interface IBaseMonitor<T extends IAEStack>
{

	/**
	 * add a new Listener to the monitor, be sure to properly remove yourself when your done.
	 */
	void addListener(IMEMonitorHandlerReceiver<T> l, Object verificationToken);

	/**
	 * remove a Listener to the monitor.
	 */
	void removeListener(IMEMonitorHandlerReceiver<T> l);

}
