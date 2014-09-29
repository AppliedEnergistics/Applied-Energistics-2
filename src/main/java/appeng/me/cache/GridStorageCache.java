package appeng.me.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.ICellProvider;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.helpers.GenericInterestManager;
import appeng.me.storage.ItemWatcher;
import appeng.me.storage.NetworkInventoryHandler;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class GridStorageCache implements IStorageGrid
{

	final private SetMultimap<IAEStack, ItemWatcher> interests = HashMultimap.create();
	final public GenericInterestManager<ItemWatcher> interestManager = new GenericInterestManager<ItemWatcher>( interests );

	final HashSet<ICellProvider> activeCellProviders = new HashSet<ICellProvider>();
	final HashSet<ICellProvider> inactiveCellProviders = new HashSet<ICellProvider>();
	final public IGrid myGrid;

	private NetworkInventoryHandler<IAEItemStack> myItemNetwork;
	private NetworkMonitor<IAEItemStack> itemMonitor = new NetworkMonitor<IAEItemStack>( this, StorageChannel.ITEMS );

	private NetworkInventoryHandler<IAEFluidStack> myFluidNetwork;
	private NetworkMonitor<IAEFluidStack> fluidMonitor = new NetworkMonitor<IAEFluidStack>( this, StorageChannel.FLUIDS );

	private HashMap<IGridNode, IStackWatcher> watchers = new HashMap<IGridNode, IStackWatcher>();

	public GridStorageCache(IGrid g) {
		myGrid = g;
	}

	@Override
	public void onUpdateTick()
	{
		itemMonitor.onTick();
		fluidMonitor.onTick();
	}

	private class CellChangeTrackerRecord
	{

		final StorageChannel channel;
		final int up_or_down;
		final IItemList list;
		final BaseActionSource src;

		public CellChangeTrackerRecord(StorageChannel channel, int i, IMEInventoryHandler<? extends IAEStack> h, BaseActionSource actionSrc) {
			this.channel = channel;
			this.up_or_down = i;
			this.src = actionSrc;

			if ( channel == StorageChannel.ITEMS )
				this.list = ((IMEInventoryHandler<IAEItemStack>) h).getAvailableItems( AEApi.instance().storage().createItemList() );
			else if ( channel == StorageChannel.FLUIDS )
				this.list = ((IMEInventoryHandler<IAEFluidStack>) h).getAvailableItems( AEApi.instance().storage().createFluidList() );
			else
				this.list = null;
		}

		public void applyChanges()
		{
			postChangesToNetwork( channel, up_or_down, list, src );
		}

	}

	private class CellChangeTracker
	{

		List<CellChangeTrackerRecord> data = new LinkedList<CellChangeTrackerRecord>();

		public void postChanges(StorageChannel channel, int i, IMEInventoryHandler<? extends IAEStack> h, BaseActionSource actionSrc)
		{
			data.add( new CellChangeTrackerRecord( channel, i, h, actionSrc ) );
		}

		public void applyChanges()
		{
			for (CellChangeTrackerRecord rec : data)
				rec.applyChanges();
		}
	}

	@Override
	public void registerCellProvider(ICellProvider provider)
	{
		inactiveCellProviders.add( provider );
		addCellProvider( provider, new CellChangeTracker() ).applyChanges();
	}

	@Override
	public void unregisterCellProvider(ICellProvider provider)
	{
		removeCellProvider( provider, new CellChangeTracker() ).applyChanges();
		inactiveCellProviders.remove( provider );
	}

	public CellChangeTracker addCellProvider(ICellProvider cc, CellChangeTracker tracker)
	{
		if ( inactiveCellProviders.contains( cc ) )
		{
			inactiveCellProviders.remove( cc );
			activeCellProviders.add( cc );

			BaseActionSource actionSrc = new BaseActionSource();
			if ( cc instanceof IActionHost )
				actionSrc = new MachineSource( (IActionHost) cc );

			for (IMEInventoryHandler<IAEItemStack> h : cc.getCellArray( StorageChannel.ITEMS ))
			{
				tracker.postChanges( StorageChannel.ITEMS, 1, h, actionSrc );
			}

			for (IMEInventoryHandler<IAEFluidStack> h : cc.getCellArray( StorageChannel.FLUIDS ))
			{
				tracker.postChanges( StorageChannel.FLUIDS, 1, h, actionSrc );
			}
		}

		return tracker;
	}

	public CellChangeTracker removeCellProvider(ICellProvider cc, CellChangeTracker tracker)
	{
		if ( activeCellProviders.contains( cc ) )
		{
			inactiveCellProviders.add( cc );
			activeCellProviders.remove( cc );

			BaseActionSource actionSrc = new BaseActionSource();
			if ( cc instanceof IActionHost )
				actionSrc = new MachineSource( (IActionHost) cc );

			for (IMEInventoryHandler<IAEItemStack> h : cc.getCellArray( StorageChannel.ITEMS ))
			{
				tracker.postChanges( StorageChannel.ITEMS, -1, h, actionSrc );
			}

			for (IMEInventoryHandler<IAEFluidStack> h : cc.getCellArray( StorageChannel.FLUIDS ))
			{
				tracker.postChanges( StorageChannel.FLUIDS, -1, h, actionSrc );
			}
		}

		return tracker;
	}

	@MENetworkEventSubscribe
	public void cellUpdate(MENetworkCellArrayUpdate ev)
	{
		myItemNetwork = null;
		myFluidNetwork = null;

		LinkedList<ICellProvider> ll = new LinkedList();
		ll.addAll( inactiveCellProviders );
		ll.addAll( activeCellProviders );

		CellChangeTracker tracker = new CellChangeTracker();

		for (ICellProvider cc : ll)
		{
			boolean Active = true;

			if ( cc instanceof IActionHost )
			{
				IGridNode node = ((IActionHost) cc).getActionableNode();
				if ( node != null && node.isActive() )
					Active = true;
				else
					Active = false;
			}

			if ( Active )
				addCellProvider( cc, tracker );
			else
				removeCellProvider( cc, tracker );
		}

		itemMonitor.forceUpdate();
		fluidMonitor.forceUpdate();

		tracker.applyChanges();
	}

	@Override
	public void removeNode(IGridNode node, IGridHost machine)
	{
		if ( machine instanceof ICellContainer )
		{
			ICellContainer cc = (ICellContainer) machine;

			myGrid.postEvent( new MENetworkCellArrayUpdate() );
			removeCellProvider( cc, new CellChangeTracker() ).applyChanges();
			inactiveCellProviders.remove( cc );
		}

		if ( machine instanceof IStackWatcherHost )
		{
			IStackWatcher myWatcher = watchers.get( machine );
			if ( myWatcher != null )
			{
				myWatcher.clear();
				watchers.remove( machine );
			}
		}
	}

	@Override
	public void addNode(IGridNode node, IGridHost machine)
	{
		if ( machine instanceof ICellContainer )
		{
			ICellContainer cc = (ICellContainer) machine;
			inactiveCellProviders.add( cc );

			myGrid.postEvent( new MENetworkCellArrayUpdate() );
			if ( node.isActive() )
				addCellProvider( cc, new CellChangeTracker() ).applyChanges();
		}

		if ( machine instanceof IStackWatcherHost )
		{
			IStackWatcherHost swh = (IStackWatcherHost) machine;
			ItemWatcher iw = new ItemWatcher( this, (IStackWatcherHost) swh );
			watchers.put( node, iw );
			swh.updateWatcher( iw );
		}
	}

	private void buildNetworkStorage(StorageChannel chan)
	{
		SecurityCache security = myGrid.getCache( ISecurityGrid.class );

		switch (chan)
		{
		case FLUIDS:
			myFluidNetwork = new NetworkInventoryHandler<IAEFluidStack>( StorageChannel.FLUIDS, security );
			for (ICellProvider cc : activeCellProviders)
			{
				for (IMEInventoryHandler<IAEFluidStack> h : cc.getCellArray( chan ))
					myFluidNetwork.addNewStorage( h );
			}
			break;
		case ITEMS:
			myItemNetwork = new NetworkInventoryHandler<IAEItemStack>( StorageChannel.ITEMS, security );
			for (ICellProvider cc : activeCellProviders)
			{
				for (IMEInventoryHandler<IAEItemStack> h : cc.getCellArray( chan ))
					myItemNetwork.addNewStorage( h );
			}
			break;
		default:
		}
	}

	private void postChangesToNetwork(StorageChannel chan, int up_or_down, IItemList availableItems, BaseActionSource src)
	{
		switch (chan)
		{
		case FLUIDS:
			fluidMonitor.postChange( up_or_down > 0, (IItemList<IAEFluidStack>) availableItems, src );
			break;
		case ITEMS:
			itemMonitor.postChange( up_or_down > 0, (IItemList<IAEItemStack>) availableItems, src );
			break;
		default:
		}
	}

	public IMEInventoryHandler<IAEItemStack> getItemInventoryHandler()
	{
		if ( myItemNetwork == null )
			buildNetworkStorage( StorageChannel.ITEMS );
		return myItemNetwork;
	}

	public IMEInventoryHandler<IAEFluidStack> getFluidInventoryHandler()
	{
		if ( myFluidNetwork == null )
			buildNetworkStorage( StorageChannel.FLUIDS );
		return myFluidNetwork;
	}

	@Override
	public void postAlterationOfStoredItems(StorageChannel chan, Iterable<? extends IAEStack> input, BaseActionSource src)
	{
		if ( chan == StorageChannel.ITEMS )
			itemMonitor.postChange( true, (Iterable<IAEItemStack>) input, src );
		else if ( chan == StorageChannel.FLUIDS )
			fluidMonitor.postChange( true, (Iterable<IAEFluidStack>) input, src );
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		return fluidMonitor;
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		return itemMonitor;
	}

	@Override
	public void onSplit(IGridStorage storageB)
	{

	}

	@Override
	public void onJoin(IGridStorage storageB)
	{

	}

	@Override
	public void populateGridStorage(IGridStorage storage)
	{

	}

}
