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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import net.minecraft.world.World;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingCallback;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingWatcher;
import appeng.api.networking.crafting.ICraftingWatcherHost;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCraftingCpuChange;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPostCacheConstruction;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ICellProvider;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.crafting.CraftingJob;
import appeng.crafting.CraftingLink;
import appeng.crafting.CraftingLinkNexus;
import appeng.crafting.CraftingWatcher;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.GenericInterestManager;
import appeng.tile.crafting.TileCraftingStorageTile;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.ItemSorters;


public class CraftingGridCache implements ICraftingGrid, ICraftingProviderHelper, ICellProvider, IMEInventoryHandler<IAEStack>
{

	final public static ExecutorService CRAFTING_POOL;
	static final Comparator<ICraftingPatternDetails> COMPARATOR = new Comparator<ICraftingPatternDetails>()
	{
		@Override
		public int compare( ICraftingPatternDetails firstDetail, ICraftingPatternDetails nextDetail )
		{
			return nextDetail.getPriority() - firstDetail.getPriority();
		}
	};

	static
	{
		ThreadFactory factory = new ThreadFactory()
		{

			@Override
			public Thread newThread( Runnable ar )
			{
				return new Thread( ar, "AE Crafting Calculator" );
			}
		};

		CRAFTING_POOL = Executors.newCachedThreadPool( factory );
	}

	private final Set<CraftingCPUCluster> craftingCPUClusters = new HashSet<CraftingCPUCluster>();
	private final Set<ICraftingProvider> craftingProviders = new HashSet<ICraftingProvider>();
	private final Map<IGridNode, ICraftingWatcher> craftingWatchers = new HashMap<IGridNode, ICraftingWatcher>();
	private final IGrid grid;
	private final Map<ICraftingPatternDetails, List<ICraftingMedium>> craftingMethods = new HashMap<ICraftingPatternDetails, List<ICraftingMedium>>();
	private final Map<IAEItemStack, ImmutableList<ICraftingPatternDetails>> craftableItems = new HashMap<IAEItemStack, ImmutableList<ICraftingPatternDetails>>();
	private final Set<IAEItemStack> emitableItems = new HashSet<IAEItemStack>();
	private final Map<String, CraftingLinkNexus> craftingLinks = new HashMap<String, CraftingLinkNexus>();
	private final Multimap<IAEStack, CraftingWatcher> interests = HashMultimap.create();
	public final GenericInterestManager<CraftingWatcher> interestManager = new GenericInterestManager<CraftingWatcher>( this.interests );
	private IStorageGrid storageGrid;
	private IEnergyGrid energyGrid;
	private boolean updateList = false;

	public CraftingGridCache( IGrid grid )
	{
		this.grid = grid;
	}

	@MENetworkEventSubscribe
	public void afterCacheConstruction( MENetworkPostCacheConstruction cacheConstruction )
	{
		this.storageGrid = this.grid.getCache( IStorageGrid.class );
		this.energyGrid = this.grid.getCache( IEnergyGrid.class );

		this.storageGrid.registerCellProvider( this );
	}

	@Override
	public void onUpdateTick()
	{
		if( this.updateList )
		{
			this.updateList = false;
			this.updateCPUClusters();
		}

		Iterator<CraftingLinkNexus> craftingLinkIterator = this.craftingLinks.values().iterator();
		while( craftingLinkIterator.hasNext() )
		{
			if( craftingLinkIterator.next().isDead( this.grid, this ) )
			{
				craftingLinkIterator.remove();
			}
		}

		for( CraftingCPUCluster cpu : this.craftingCPUClusters )
		{
			cpu.updateCraftingLogic( this.grid, this.energyGrid, this );
		}
	}

	@Override
	public void removeNode( IGridNode gridNode, IGridHost machine )
	{
		if( machine instanceof ICraftingWatcherHost )
		{
			ICraftingWatcher craftingWatcher = this.craftingWatchers.get( machine );
			if( craftingWatcher != null )
			{
				craftingWatcher.clear();
				this.craftingWatchers.remove( machine );
			}
		}

		if( machine instanceof ICraftingRequester )
		{
			for( CraftingLinkNexus link : this.craftingLinks.values() )
			{
				if( link.isMachine( machine ) )
				{
					link.removeNode();
				}
			}
		}

		if( machine instanceof TileCraftingTile )
		{
			this.updateList = true;
		}

		if( machine instanceof ICraftingProvider )
		{
			this.craftingProviders.remove( machine );
			this.updatePatterns();
		}
	}

	@Override
	public void addNode( IGridNode gridNode, IGridHost machine )
	{
		if( machine instanceof ICraftingWatcherHost )
		{
			ICraftingWatcherHost watcherHost = (ICraftingWatcherHost) machine;
			CraftingWatcher watcher = new CraftingWatcher( this, watcherHost );
			this.craftingWatchers.put( gridNode, watcher );
			watcherHost.updateWatcher( watcher );
		}

		if( machine instanceof ICraftingRequester )
		{
			for( ICraftingLink link : ( (ICraftingRequester) machine ).getRequestedJobs() )
			{
				if( link instanceof CraftingLink )
				{
					this.addLink( (CraftingLink) link );
				}
			}
		}

		if( machine instanceof TileCraftingTile )
		{
			this.updateList = true;
		}

		if( machine instanceof ICraftingProvider )
		{
			this.craftingProviders.add( (ICraftingProvider) machine );
			this.updatePatterns();
		}
	}

	@Override
	public void onSplit( IGridStorage destinationStorage )
	{ // nothing!
	}

	@Override
	public void onJoin( IGridStorage sourceStorage )
	{
		// nothing!
	}

	@Override
	public void populateGridStorage( IGridStorage destinationStorage )
	{
		// nothing!
	}

	private void updateCPUClusters()
	{
		this.craftingCPUClusters.clear();

		for( IGridNode cst : this.grid.getMachines( TileCraftingStorageTile.class ) )
		{
			TileCraftingStorageTile tile = (TileCraftingStorageTile) cst.getMachine();
			CraftingCPUCluster cluster = (CraftingCPUCluster) tile.getCluster();
			if( cluster != null )
			{
				this.craftingCPUClusters.add( cluster );

				if( cluster.myLastLink != null )
				{
					this.addLink( (CraftingLink) cluster.myLastLink );
				}
			}
		}
	}

	public void addLink( CraftingLink link )
	{
		if( link.isStandalone() )
		{
			return;
		}

		CraftingLinkNexus nexus = this.craftingLinks.get( link.getCraftingID() );
		if( nexus == null )
		{
			this.craftingLinks.put( link.getCraftingID(), nexus = new CraftingLinkNexus( link.getCraftingID() ) );
		}

		link.setNexus( nexus );
	}

	@MENetworkEventSubscribe
	public void updateCPUClusters( MENetworkCraftingCpuChange c )
	{
		this.updateList = true;
	}

	@MENetworkEventSubscribe
	public void updateCPUClusters( MENetworkCraftingPatternChange c )
	{
		this.updatePatterns();
	}

	private void updatePatterns()
	{
		Map<IAEItemStack, ImmutableList<ICraftingPatternDetails>> oldItems = this.craftableItems;

		// erase list.
		this.craftingMethods.clear();
		this.craftableItems.clear();
		this.emitableItems.clear();

		// update the stuff that was in the list...
		this.storageGrid.postAlterationOfStoredItems( StorageChannel.ITEMS, oldItems.keySet(), new BaseActionSource() );

		// re-create list..
		for( ICraftingProvider provider : this.craftingProviders )
		{
			provider.provideCrafting( this );
		}

		Map<IAEItemStack, Set<ICraftingPatternDetails>> tmpCraft = new HashMap<IAEItemStack, Set<ICraftingPatternDetails>>();

		// new craftables!
		for( ICraftingPatternDetails details : this.craftingMethods.keySet() )
		{
			for( IAEItemStack out : details.getOutputs() )
			{
				out = out.copy();
				out.reset();
				out.setCraftable( true );

				Set<ICraftingPatternDetails> methods = tmpCraft.get( out );

				if( methods == null )
				{
					tmpCraft.put( out, methods = new TreeSet<ICraftingPatternDetails>( COMPARATOR ) );
				}

				methods.add( details );
			}
		}

		// make them immutable
		for( Entry<IAEItemStack, Set<ICraftingPatternDetails>> e : tmpCraft.entrySet() )
		{
			this.craftableItems.put( e.getKey(), ImmutableList.copyOf( e.getValue() ) );
		}

		this.storageGrid.postAlterationOfStoredItems( StorageChannel.ITEMS, this.craftableItems.keySet(), new BaseActionSource() );
	}

	@Override
	public void addCraftingOption( ICraftingMedium medium, ICraftingPatternDetails api )
	{
		List<ICraftingMedium> details = this.craftingMethods.get( api );
		if( details == null )
		{
			details = new ArrayList<ICraftingMedium>();
			details.add( medium );
			this.craftingMethods.put( api, details );
		}
		else
		{
			details.add( medium );
		}
	}

	@Override
	public void setEmitable( IAEItemStack someItem )
	{
		this.emitableItems.add( someItem.copy() );
	}

	@Override
	public List<IMEInventoryHandler> getCellArray( StorageChannel channel )
	{
		List<IMEInventoryHandler> list = new ArrayList<IMEInventoryHandler>( 1 );

		if( channel == StorageChannel.ITEMS )
		{
			list.add( this );
		}

		return list;
	}

	@Override
	public int getPriority()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public AccessRestriction getAccess()
	{
		return AccessRestriction.WRITE;
	}

	@Override
	public boolean isPrioritized( IAEStack input )
	{
		return true;
	}

	@Override
	public boolean canAccept( IAEStack input )
	{
		for( CraftingCPUCluster cpu : this.craftingCPUClusters )
		{
			if( cpu.canAccept( input ) )
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public int getSlot()
	{
		return 0;
	}

	@Override
	public boolean validForPass( int i )
	{
		return i == 1;
	}

	@Override
	public IAEStack injectItems( IAEStack input, Actionable type, BaseActionSource src )
	{
		for( CraftingCPUCluster cpu : this.craftingCPUClusters )
		{
			input = cpu.injectItems( input, type, src );
		}

		return input;
	}

	@Override
	public IAEStack extractItems( IAEStack request, Actionable mode, BaseActionSource src )
	{
		return null;
	}

	@Override
	public IItemList<IAEStack> getAvailableItems( IItemList<IAEStack> out )
	{
		// add craftable items!
		for( IAEItemStack stack : this.craftableItems.keySet() )
		{
			out.addCrafting( stack );
		}

		for( IAEItemStack st : this.emitableItems )
		{
			out.addCrafting( st );
		}

		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public ImmutableCollection<ICraftingPatternDetails> getCraftingFor( IAEItemStack whatToCraft, ICraftingPatternDetails details, int slotIndex, World world )
	{
		ImmutableList<ICraftingPatternDetails> res = this.craftableItems.get( whatToCraft );

		if( res == null )
		{
			if( details != null && details.isCraftable() )
			{
				for( IAEItemStack ais : this.craftableItems.keySet() )
				{
					if( ais.getItem() == whatToCraft.getItem() && ( !ais.getItem().getHasSubtypes() || ais.getItemDamage() == whatToCraft.getItemDamage() ) )
					{
						if( details.isValidItemForSlot( slotIndex, ais.getItemStack(), world ) )
						{
							return this.craftableItems.get( ais );
						}
					}
				}
			}

			return ImmutableSet.of();
		}

		return res;
	}

	@Override
	public Future<ICraftingJob> beginCraftingJob( World world, IGrid grid, BaseActionSource actionSrc, IAEItemStack slotItem, ICraftingCallback cb )
	{
		if( world == null || grid == null || actionSrc == null || slotItem == null )
		{
			throw new RuntimeException( "Invalid Crafting Job Request" );
		}

		CraftingJob job = new CraftingJob( world, grid, actionSrc, slotItem, cb );

		return CRAFTING_POOL.submit( job, (ICraftingJob) job );
	}

	@Override
	public ICraftingLink submitJob( ICraftingJob job, ICraftingRequester requestingMachine, ICraftingCPU target, final boolean prioritizePower, BaseActionSource src )
	{
		if( job.isSimulation() )
		{
			return null;
		}

		CraftingCPUCluster cpuCluster = null;

		if( target instanceof CraftingCPUCluster )
		{
			cpuCluster = (CraftingCPUCluster) target;
		}

		if( target == null )
		{
			List<CraftingCPUCluster> validCpusClusters = new ArrayList<CraftingCPUCluster>();
			for( CraftingCPUCluster cpu : this.craftingCPUClusters )
			{
				if( cpu.isActive() && !cpu.isBusy() && cpu.getAvailableStorage() >= job.getByteTotal() )
				{
					validCpusClusters.add( cpu );
				}
			}

			Collections.sort( validCpusClusters, new Comparator<CraftingCPUCluster>()
			{
				@Override
				public int compare( CraftingCPUCluster firstCluster, CraftingCPUCluster nextCluster )
				{
					if( prioritizePower )
					{
						int comparison = ItemSorters.compareLong( nextCluster.getCoProcessors(), firstCluster.getCoProcessors() );
						if( comparison != 0 )
							return comparison;
						return ItemSorters.compareLong( nextCluster.getAvailableStorage(), firstCluster.getAvailableStorage() );
					}

					int comparison = ItemSorters.compareLong( firstCluster.getCoProcessors(), nextCluster.getCoProcessors() );
					if( comparison != 0 )
						return comparison;
					return ItemSorters.compareLong( firstCluster.getAvailableStorage(), nextCluster.getAvailableStorage() );
				}
			} );

			if( !validCpusClusters.isEmpty() )
			{
				cpuCluster = validCpusClusters.get( 0 );
			}
		}

		if( cpuCluster != null )
		{
			return cpuCluster.submitJob( this.grid, job, src, requestingMachine );
		}

		return null;
	}

	@Override
	public ImmutableSet<ICraftingCPU> getCpus()
	{
		return ImmutableSet.copyOf( new ActiveCpuIterator( this.craftingCPUClusters ) );
	}

	@Override
	public boolean canEmitFor( IAEItemStack someItem )
	{
		return this.emitableItems.contains( someItem );
	}

	@Override
	public boolean isRequesting( IAEItemStack what )
	{
		for( CraftingCPUCluster cluster : this.craftingCPUClusters )
		{
			if( cluster.isMaking( what ) )
			{
				return true;
			}
		}

		return false;
	}

	public List<ICraftingMedium> getMediums( ICraftingPatternDetails key )
	{
		List<ICraftingMedium> mediums = this.craftingMethods.get( key );

		if( mediums == null )
		{
			mediums = ImmutableList.of();
		}

		return mediums;
	}

	public boolean hasCpu( ICraftingCPU cpu )
	{
		return this.craftingCPUClusters.contains( cpu );
	}


	static class ActiveCpuIterator implements Iterator<ICraftingCPU>
	{

		private final Iterator<CraftingCPUCluster> iterator;
		private CraftingCPUCluster cpuCluster;

		public ActiveCpuIterator( Collection<CraftingCPUCluster> o )
		{
			this.iterator = o.iterator();
			this.cpuCluster = null;
		}

		@Override
		public boolean hasNext()
		{
			this.findNext();

			return this.cpuCluster != null;
		}

		private void findNext()
		{
			while( this.iterator.hasNext() && this.cpuCluster == null )
			{
				this.cpuCluster = this.iterator.next();
				if( !this.cpuCluster.isActive() || this.cpuCluster.isDestroyed )
				{
					this.cpuCluster = null;
				}
			}
		}

		@Override
		public ICraftingCPU next()
		{
			final ICraftingCPU o = this.cpuCluster;
			this.cpuCluster = null;

			return o;
		}

		@Override
		public void remove()
		{
			// no..
		}
	}
}
