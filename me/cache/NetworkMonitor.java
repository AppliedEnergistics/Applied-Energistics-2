package appeng.me.cache;

import java.util.LinkedList;
import java.util.Set;

import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.storage.ItemWatcher;

public class NetworkMonitor<T extends IAEStack<T>> extends MEMonitorHandler<T>
{

	final private GridStorageCache myGridCache;
	final private StorageChannel myChannel;

	boolean sendEvent = false;

	public void forceUpdate()
	{
		hasChanged = true;
		sendEvent = true;
	}

	public NetworkMonitor(GridStorageCache cache, StorageChannel chan) {
		super( null, chan );
		myGridCache = cache;
		myChannel = chan;
	}

	final private LinkedList<ChangeRecord<T>> changes = new LinkedList();

	class ChangeRecord<G extends IAEStack<T>>
	{
		
		public ChangeRecord(G diff2, BaseActionSource src2) {
			diff = diff2;
			src = src2;
		}
		
		G diff;
		BaseActionSource src;
		
	};

	@Override
	protected void postChange(T diff, BaseActionSource src)
	{
		sendEvent = true;
		hasChanged = true;
		changes.add( new ChangeRecord( diff, src ) );
	}

	public void onTick()
	{
		if ( sendEvent )
		{
			sendEvent = false;
			
			ChangeRecord<T> cr;
			while ( (cr=changes.poll()) != null )
			{
				T diff = cr.diff;
				BaseActionSource src = cr.src;
				
				IItemList<T> myStorageList = getStorageList();				
				
				postChangeToListeners( diff, src );
				
				if ( myGridCache.interestManager.containsKey( diff ) )
				{
					Set<ItemWatcher> list = myGridCache.interestManager.get( diff );
					if ( !list.isEmpty() )
					{
						IAEStack fullStack = myStorageList.findPrecise( diff );
						if ( fullStack == null )
						{
							fullStack = diff.copy();
							fullStack.setStackSize( 0 );
						}
						
						myGridCache.interestManager.enableTransactions();
						
						for (ItemWatcher iw : list)
							iw.getHost().onStackChange( myStorageList, fullStack, diff, src, getChannel() );
						
						myGridCache.interestManager.disableTransactions();
					}
				}
			}
			
			myGridCache.myGrid.postEvent( new MENetworkStorageEvent( this, myChannel ) );
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
