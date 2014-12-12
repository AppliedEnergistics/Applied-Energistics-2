package appeng.api.storage;

import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.data.IAEStack;

public interface IMEMonitorHandlerReceiver<StackType extends IAEStack>
{

	/**
	 * return true if this object should remain as a listener.
	 * 
	 * @param verificationToken to be checked object
	 * @return true if object should remain as a listener
	 */
	boolean isValid(Object verificationToken);

	/**
	 * called when changes are made to the Monitor, but only if listener is still valid.
	 * 
	 * @param change done change
	 */
	void postChange(IBaseMonitor<StackType> monitor, Iterable<StackType> change, BaseActionSource actionSource);

	/**
	 * called when the list updates its contents, this is mostly for handling power events.
	 */
	void onListUpdate();

}
