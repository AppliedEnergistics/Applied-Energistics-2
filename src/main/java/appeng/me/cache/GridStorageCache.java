/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.me.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

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

public class GridStorageCache implements IStorageGrid
{

	final private SetMultimap<IAEStack, ItemWatcher> interests = HashMultimap.create();
	final public GenericInterestManager<ItemWatcher> interestManager = new GenericInterestManager<ItemWatcher>( this.interests );

	final HashSet<ICellProvider> activeCellProviders = new HashSet<ICellProvider>();
	final HashSet<ICellProvider> inactiveCellProviders = new HashSet<ICellProvider>();
	final public IGrid myGrid;

	private NetworkInventoryHandler<IAEItemStack> myItemNetwork;
	private final NetworkMonitor<IAEItemStack> itemMonitor = new NetworkMonitor<IAEItemStack>( this, StorageChannel.ITEMS );

	private NetworkInventoryHandler<IAEFluidStack> myFluidNetwork;
	private final NetworkMonitor<IAEFluidStack> fluidMonitor = new NetworkMonitor<IAEFluidStack>( this, StorageChannel.FLUIDS );

	private final HashMap<IGridNode, IStackWatcher> watchers = new HashMap<IGridNode, IStackWatcher>();

	public GridStorageCache(IGrid g) {
		this.myGrid = g;
	}

	@Override
	public void onUpdateTick()
	{
		this.itemMonitor.onTick();
		this.fluidMonitor.onTick();
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
			GridStorageCache.this.postChangesToNetwork( this.channel, this.up_or_down, this.list, this.src );
		}

	}

	private class CellChangeTracker
	{

		final List<CellChangeTrackerRecord> data = new LinkedList<CellChangeTrackerRecord>();

		public void postChanges(StorageChannel channel, int i, IMEInventoryHandler<? extends IAEStack> h, BaseActionSource actionSrc)
		{
			this.data.add( new CellChangeTrackerRecord( channel, i, h, actionSrc ) );
		}

		public void applyChanges()
		{
			for (CellChangeTrackerRecord rec : this.data)
				rec.applyChanges();
		}
	}

	@Override
	public void registerCellProvider(ICellProvider provider)
	{
		this.inactiveCellProviders.add( provider );
		this.addCellProvider( provider, new CellChangeTracker() ).applyChanges();
	}

	@Override
	public void unregisterCellProvider(ICellProvider provider)
	{
		this.removeCellProvider( provider, new CellChangeTracker() ).applyChanges();
		this.inactiveCellProviders.remove( provider );
	}

	public CellChangeTracker addCellProvider(ICellProvider cc, CellChangeTracker tracker)
	{
		if ( this.inactiveCellProviders.contains( cc ) )
		{
			this.inactiveCellProviders.remove( cc );
			this.activeCellProviders.add( cc );

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
		if ( this.activeCellProviders.contains( cc ) )
		{
			this.inactiveCellProviders.add( cc );
			this.activeCellProviders.remove( cc );

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
		this.myItemNetwork = null;
		this.myFluidNetwork = null;

		LinkedList<ICellProvider> ll = new LinkedList();
		ll.addAll( this.inactiveCellProviders );
		ll.addAll( this.activeCellProviders );

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
				this.addCellProvider( cc, tracker );
			else
				this.removeCellProvider( cc, tracker );
		}

		this.itemMonitor.forceUpdate();
		this.fluidMonitor.forceUpdate();

		tracker.applyChanges();
	}

	@Override
	public void removeNode(IGridNode node, IGridHost machine)
	{
		if ( machine instanceof ICellContainer )
		{
			ICellContainer cc = (ICellContainer) machine;

			this.myGrid.postEvent( new MENetworkCellArrayUpdate() );
			this.removeCellProvider( cc, new CellChangeTracker() ).applyChanges();
			this.inactiveCellProviders.remove( cc );
		}

		if ( machine instanceof IStackWatcherHost )
		{
			IStackWatcher myWatcher = this.watchers.get( machine );
			if ( myWatcher != null )
			{
				myWatcher.clear();
				this.watchers.remove( machine );
			}
		}
	}

	@Override
	public void addNode(IGridNode node, IGridHost machine)
	{
		if ( machine instanceof ICellContainer )
		{
			ICellContainer cc = (ICellContainer) machine;
			this.inactiveCellProviders.add( cc );

			this.myGrid.postEvent( new MENetworkCellArrayUpdate() );
			if ( node.isActive() )
				this.addCellProvider( cc, new CellChangeTracker() ).applyChanges();
		}

		if ( machine instanceof IStackWatcherHost )
		{
			IStackWatcherHost swh = (IStackWatcherHost) machine;
			ItemWatcher iw = new ItemWatcher( this, swh );
			this.watchers.put( node, iw );
			swh.updateWatcher( iw );
		}
	}

	private void buildNetworkStorage(StorageChannel chan)
	{
		SecurityCache security = this.myGrid.getCache( ISecurityGrid.class );

		switch (chan)
		{
		case FLUIDS:
			this.myFluidNetwork = new NetworkInventoryHandler<IAEFluidStack>( StorageChannel.FLUIDS, security );
			for (ICellProvider cc : this.activeCellProviders)
			{
				for (IMEInventoryHandler<IAEFluidStack> h : cc.getCellArray( chan ))
					this.myFluidNetwork.addNewStorage( h );
			}
			break;
		case ITEMS:
			this.myItemNetwork = new NetworkInventoryHandler<IAEItemStack>( StorageChannel.ITEMS, security );
			for (ICellProvider cc : this.activeCellProviders)
			{
				for (IMEInventoryHandler<IAEItemStack> h : cc.getCellArray( chan ))
					this.myItemNetwork.addNewStorage( h );
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
			this.fluidMonitor.postChange( up_or_down > 0, availableItems, src );
			break;
		case ITEMS:
			this.itemMonitor.postChange( up_or_down > 0, availableItems, src );
			break;
		default:
		}
	}

	public IMEInventoryHandler<IAEItemStack> getItemInventoryHandler()
	{
		if ( this.myItemNetwork == null )
			this.buildNetworkStorage( StorageChannel.ITEMS );
		return this.myItemNetwork;
	}

	public IMEInventoryHandler<IAEFluidStack> getFluidInventoryHandler()
	{
		if ( this.myFluidNetwork == null )
			this.buildNetworkStorage( StorageChannel.FLUIDS );
		return this.myFluidNetwork;
	}

	@Override
	public void postAlterationOfStoredItems(StorageChannel chan, Iterable<? extends IAEStack> input, BaseActionSource src)
	{
		if ( chan == StorageChannel.ITEMS )
			this.itemMonitor.postChange( true, (Iterable<IAEItemStack>) input, src );
		else if ( chan == StorageChannel.FLUIDS )
			this.fluidMonitor.postChange( true, (Iterable<IAEFluidStack>) input, src );
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		return this.fluidMonitor;
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		return this.itemMonitor;
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
