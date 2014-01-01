package appeng.me.storage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReciever;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import appeng.util.item.ItemList;

public class MEMonitorPassthu<T extends IAEStack<T>> extends MEPassthru<T> implements IMEMonitor<T>, IMEMonitorHandlerReciever<T>
{

	HashMap<IMEMonitorHandlerReciever<T>, Object> listeners = new HashMap();
	IMEMonitor<T> monitor;

	public MEMonitorPassthu(IMEInventory<T> i) {
		super( i );
		if ( i instanceof IMEMonitor )
			monitor = (IMEMonitor<T>) i;
	}

	@Override
	public void setInternal(IMEInventory<T> i)
	{
		if ( monitor != null )
			monitor.removeListener( this );

		monitor = null;
		IItemList<T> before = getInternal() == null ? new ItemList() : getInternal().getAvailableItems( new ItemList() );

		super.setInternal( i );
		if ( i instanceof IMEMonitor )
			monitor = (IMEMonitor<T>) i;

		IItemList<T> after = getInternal() == null ? new ItemList() : getInternal().getAvailableItems( new ItemList() );

		if ( monitor != null )
			monitor.addListener( this, monitor );

		Platform.postListChanges( before, after, this );
	}

	@Override
	public void addListener(IMEMonitorHandlerReciever<T> l, Object verificationToken)
	{
		listeners.put( l, verificationToken );
	}

	@Override
	public void removeListener(IMEMonitorHandlerReciever<T> l)
	{
		listeners.remove( l );
	}

	@Override
	public IItemList<T> getStorageList()
	{
		if ( monitor == null )
			return getInternal().getAvailableItems( new ItemList<T>() );
		return monitor.getStorageList();
	}

	@Override
	public boolean isValid(Object verificationToken)
	{
		return verificationToken == monitor;
	}

	@Override
	public void postChange(T change)
	{
		Iterator<Entry<IMEMonitorHandlerReciever<T>, Object>> i = listeners.entrySet().iterator();
		while (i.hasNext())
		{
			Entry<IMEMonitorHandlerReciever<T>, Object> e = i.next();
			IMEMonitorHandlerReciever<T> recv = e.getKey();
			if ( recv.isValid( e.getValue() ) )
				recv.postChange( change );
			else
				i.remove();
		}
	}
}
