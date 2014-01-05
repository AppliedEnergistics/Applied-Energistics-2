package appeng.me.cache;

import java.util.HashMap;
import java.util.HashSet;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.storage.ItemWatcher;
import appeng.me.storage.NetworkInventoryHandler;
import appeng.util.item.ItemList;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class GridStorageCache implements IStorageGrid
{

	public SetMultimap<IAEStack, ItemWatcher> interests = HashMultimap.create();

	final HashSet<ICellContainer> cellContainers = new HashSet();
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
	public void onUpdateTick(IGrid grid)
	{
		itemMonitor.onTick();
		fluidMonitor.onTick();
	}

	@Override
	public void removeNode(IGrid grid, IGridNode node, IGridHost machine)
	{
		if ( machine instanceof ICellContainer )
		{
			ICellContainer cc = (ICellContainer) machine;
			cellContainers.remove( cc );

			myGrid.postEvent( new MENetworkCellArrayUpdate() );

			for (IMEInventoryHandler<IAEItemStack> h : cc.getCellArray( StorageChannel.ITEMS ))
			{
				postChanges( StorageChannel.ITEMS, -1, h.getAvailableItems( new ItemList<IAEItemStack>() ), new MachineSource( machine ) );
			}

			for (IMEInventoryHandler<IAEFluidStack> h : cc.getCellArray( StorageChannel.FLUIDS ))
			{
				postChanges( StorageChannel.ITEMS, -1, h.getAvailableItems( new ItemList<IAEFluidStack>() ), new MachineSource( machine ) );
			}
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
	public void addNode(IGrid grid, IGridNode node, IGridHost machine)
	{
		if ( machine instanceof ICellContainer )
		{
			ICellContainer cc = (ICellContainer) machine;
			cellContainers.add( cc );

			myGrid.postEvent( new MENetworkCellArrayUpdate() );

			for (IMEInventoryHandler<IAEItemStack> h : cc.getCellArray( StorageChannel.ITEMS ))
			{
				postChanges( StorageChannel.ITEMS, 1, h.getAvailableItems( new ItemList<IAEItemStack>() ), new MachineSource( machine ) );
			}

			for (IMEInventoryHandler<IAEFluidStack> h : cc.getCellArray( StorageChannel.FLUIDS ))
			{
				postChanges( StorageChannel.ITEMS, 1, h.getAvailableItems( new ItemList<IAEFluidStack>() ), new MachineSource( machine ) );
			}
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
		switch (chan)
		{
		case FLUIDS:
			myFluidNetwork = new NetworkInventoryHandler<IAEFluidStack>( StorageChannel.FLUIDS );
			for (ICellContainer cc : cellContainers)
			{
				for (IMEInventoryHandler<IAEFluidStack> h : cc.getCellArray( chan ))
					myFluidNetwork.addNewStorage( h );
			}
			break;
		case ITEMS:
			myItemNetwork = new NetworkInventoryHandler<IAEItemStack>( StorageChannel.ITEMS );
			for (ICellContainer cc : cellContainers)
			{
				for (IMEInventoryHandler<IAEItemStack> h : cc.getCellArray( chan ))
					myItemNetwork.addNewStorage( h );
			}
			break;
		default:
		}
	}

	private void postChanges(StorageChannel chan, int up_or_down, IItemList availableItems, BaseActionSource src)
	{
		switch (chan)
		{
		case FLUIDS:
			for (IAEFluidStack fs : ((IItemList<IAEFluidStack>) availableItems))
			{
				if ( up_or_down > 0 )
					fluidMonitor.postChange( fs, src );
				else
				{
					fs.setStackSize( -fs.getStackSize() );
					fluidMonitor.postChange( fs, src );
				}
			}
			break;
		case ITEMS:
			for (IAEItemStack fs : ((IItemList<IAEItemStack>) availableItems))
			{
				if ( up_or_down > 0 )
					itemMonitor.postChange( fs, src );
				else
				{
					fs.setStackSize( -fs.getStackSize() );
					itemMonitor.postChange( fs, src );
				}
			}
			break;
		default:
		}
	}

	@MENetworkEventSubscribe
	public void cellUpdate(MENetworkCellArrayUpdate ev)
	{
		myItemNetwork = null;
		myFluidNetwork = null;

		itemMonitor.forceUpdate();
		fluidMonitor.forceUpdate();
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
	public void postAlterationOfStoredItems(StorageChannel chan, IAEStack input, BaseActionSource src)
	{
		if ( chan == StorageChannel.ITEMS )
			itemMonitor.postChange( (IAEItemStack) input, src );
		else if ( chan == StorageChannel.FLUIDS )
			fluidMonitor.postChange( (IAEFluidStack) input, src );
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
