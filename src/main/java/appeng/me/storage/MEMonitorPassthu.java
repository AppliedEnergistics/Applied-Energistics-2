package appeng.me.storage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import appeng.util.inv.ItemListIgnoreCrafting;

public class MEMonitorPassthu<T extends IAEStack<T>> extends MEPassthru<T> implements IMEMonitor<T>, IMEMonitorHandlerReceiver<T>
{

	HashMap<IMEMonitorHandlerReceiver<T>, Object> listeners = new HashMap();
	IMEMonitor<T> monitor;

	public BaseActionSource changeSource;

	public MEMonitorPassthu(IMEInventory<T> i, StorageChannel channel) {
		super( i, channel );
		if ( i instanceof IMEMonitor )
			monitor = (IMEMonitor<T>) i;
	}

	@Override
	public void setInternal(IMEInventory<T> i)
	{
		if ( monitor != null )
			monitor.removeListener( this );

		monitor = null;
		IItemList<T> before = getInternal() == null ? channel.createList() : getInternal()
				.getAvailableItems( new ItemListIgnoreCrafting( channel.createList() ) );

		super.setInternal( i );
		if ( i instanceof IMEMonitor )
			monitor = (IMEMonitor<T>) i;

		IItemList<T> after = getInternal() == null ? channel.createList() : getInternal()
				.getAvailableItems( new ItemListIgnoreCrafting( channel.createList() ) );

		if ( monitor != null )
			monitor.addListener( this, monitor );

		Platform.postListChanges( before, after, this, changeSource );
	}

	@Override
	public IItemList<T> getAvailableItems(IItemList out)
	{
		super.getAvailableItems( new ItemListIgnoreCrafting( out ) );
		return out;
	}

	@Override
	public void addListener(IMEMonitorHandlerReceiver<T> l, Object verificationToken)
	{
		listeners.put( l, verificationToken );
	}

	@Override
	public void removeListener(IMEMonitorHandlerReceiver<T> l)
	{
		listeners.remove( l );
	}

	@Override
	public IItemList<T> getStorageList()
	{
		if ( monitor == null )
		{
			IItemList<T> out = channel.createList();
			getInternal().getAvailableItems( new ItemListIgnoreCrafting( out ) );
			return out;
		}
		return monitor.getStorageList();
	}

	@Override
	public boolean isValid(Object verificationToken)
	{
		return verificationToken == monitor;
	}

	@Override
	public void postChange(IBaseMonitor<T> monitor, Iterable<T> change, BaseActionSource source)
	{
		Iterator<Entry<IMEMonitorHandlerReceiver<T>, Object>> i = listeners.entrySet().iterator();
		while (i.hasNext())
		{
			Entry<IMEMonitorHandlerReceiver<T>, Object> e = i.next();
			IMEMonitorHandlerReceiver<T> recv = e.getKey();
			if ( recv.isValid( e.getValue() ) )
				recv.postChange( this, change, source );
			else
				i.remove();
		}
	}

	@Override
	public void onListUpdate()
	{
		Iterator<Entry<IMEMonitorHandlerReceiver<T>, Object>> i = listeners.entrySet().iterator();
		while (i.hasNext())
		{
			Entry<IMEMonitorHandlerReceiver<T>, Object> e = i.next();
			IMEMonitorHandlerReceiver<T> recv = e.getKey();
			if ( recv.isValid( e.getValue() ) )
				recv.onListUpdate();
			else
				i.remove();
		}
	}
}
