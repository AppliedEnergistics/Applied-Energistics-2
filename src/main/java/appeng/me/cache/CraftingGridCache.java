package appeng.me.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import net.minecraft.world.World;
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
import appeng.me.storage.ItemWatcher;
import appeng.tile.crafting.TileCraftingStorageTile;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.ItemSorters;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

public class CraftingGridCache implements ICraftingGrid, ICraftingProviderHelper, ICellProvider, IMEInventoryHandler
{

	HashSet<CraftingCPUCluster> cpuClusters = new HashSet<CraftingCPUCluster>();
	HashSet<ICraftingProvider> providers = new HashSet<ICraftingProvider>();

	private HashMap<IGridNode, ICraftingWatcher> watchers = new HashMap<IGridNode, ICraftingWatcher>();

	IGrid grid;
	IStorageGrid sg;
	IEnergyGrid eg;

	HashMap<ICraftingPatternDetails, List<ICraftingMedium>> craftingMethods = new HashMap<ICraftingPatternDetails, List<ICraftingMedium>>();
	HashMap<IAEItemStack, ImmutableList<ICraftingPatternDetails>> craftableItems = new HashMap<IAEItemStack, ImmutableList<ICraftingPatternDetails>>();
	HashSet<IAEItemStack> emitableItems = new HashSet<IAEItemStack>();
	HashMap<String, CraftingLinkNexus> links = new HashMap<String, CraftingLinkNexus>();

	boolean updateList = false;
	final private SetMultimap<IAEStack, CraftingWatcher> interests = HashMultimap.create();
	final public GenericInterestManager<CraftingWatcher> interestManager = new GenericInterestManager<CraftingWatcher>( interests );

	class ActiveCpuIterator implements Iterator<ICraftingCPU>
	{

		final Iterator<CraftingCPUCluster> i;
		CraftingCPUCluster c = null;

		public ActiveCpuIterator(Collection<CraftingCPUCluster> o)
		{
			i = o.iterator();
		}

		@Override
		public boolean hasNext()
		{
			findNext();
			return c != null;
		}

		private void findNext()
		{
			while (i.hasNext() && c == null)
			{
				c = i.next();
				if ( !c.isActive() || c.isDestroyed )
					c = null;
			}
		}

		@Override
		public ICraftingCPU next()
		{
			ICraftingCPU o = c;
			c = null;
			return o;
		}

		@Override
		public void remove()
		{
			// no..
		}

	}

	@Override
	public ImmutableSet<ICraftingCPU> getCpus()
	{
		return ImmutableSet.copyOf( new ActiveCpuIterator( cpuClusters ) );
	}

	public CraftingGridCache(IGrid g)
	{
		grid = g;
	}

	@MENetworkEventSubscribe
	public void afterCacheConstruction(MENetworkPostCacheConstruction cc)
	{
		sg = grid.getCache( IStorageGrid.class );
		eg = grid.getCache( IEnergyGrid.class );

		sg.registerCellProvider( this );
	}

	public void addLink(CraftingLink l)
	{
		if ( l.isStandalone() )
			return;

		CraftingLinkNexus n = links.get( l.getCraftingID() );
		if ( n == null )
			links.put( l.getCraftingID(), n = new CraftingLinkNexus( l.getCraftingID() ) );

		l.setNexus( n );
	}

	@Override
	public void onUpdateTick()
	{
		if ( updateList )
		{
			updateList = false;
			updateCPUClusters();
		}

		Iterator<CraftingLinkNexus> i = links.values().iterator();
		while (i.hasNext())
		{
			if ( i.next().isDead( grid, this ) )
				i.remove();
		}

		for (CraftingCPUCluster cpu : cpuClusters)
			cpu.updateCraftingLogic( grid, eg, this );
	}

	@MENetworkEventSubscribe
	public void updateCPUClusters(MENetworkCraftingCpuChange c)
	{
		updateList = true;
	}

	@MENetworkEventSubscribe
	public void updateCPUClusters(MENetworkCraftingPatternChange c)
	{
		updatePatterns();
	}

	@Override
	public void removeNode(IGridNode gridNode, IGridHost machine)
	{
		if ( machine instanceof ICraftingWatcherHost )
		{
			ICraftingWatcher myWatcher = watchers.get( machine );
			if ( myWatcher != null )
			{
				myWatcher.clear();
				watchers.remove( machine );
			}
		}

		if ( machine instanceof ICraftingRequester )
		{
			for (CraftingLinkNexus n : links.values())
			{
				if ( n.isMachine( machine ) )
				{
					n.removeNode();
				}
			}
		}

		if ( machine instanceof TileCraftingTile )
			updateList = true;
		if ( machine instanceof ICraftingProvider )
		{
			providers.remove( machine );
			updatePatterns();
		}
	}

	@Override
	public void addNode(IGridNode gridNode, IGridHost machine)
	{
		if ( machine instanceof ICraftingWatcherHost )
		{
			ICraftingWatcherHost swh = (ICraftingWatcherHost) machine;
			CraftingWatcher iw = new CraftingWatcher( this, swh );
			watchers.put( gridNode, iw );
			swh.updateWatcher( iw );
		}

		if ( machine instanceof ICraftingRequester )
		{
			for (ICraftingLink l : ((ICraftingRequester) machine).getRequestedJobs())
			{
				if ( l instanceof CraftingLink )
					addLink( (CraftingLink) l );
			}
		}

		if ( machine instanceof TileCraftingTile )
			updateList = true;
		if ( machine instanceof ICraftingProvider )
		{
			providers.add( (ICraftingProvider) machine );
			updatePatterns();
		}
	}

	private void updateCPUClusters()
	{
		cpuClusters.clear();
		for (IGridNode cst : grid.getMachines( TileCraftingStorageTile.class ))
		{
			TileCraftingStorageTile tile = (TileCraftingStorageTile) cst.getMachine();
			CraftingCPUCluster cluster = (CraftingCPUCluster) tile.getCluster();
			if ( cluster != null )
			{
				cpuClusters.add( cluster );

				if ( cluster.myLastLink != null )
					addLink( (CraftingLink) cluster.myLastLink );
			}
		}
	}

	@Override
	public void addCraftingOption(ICraftingMedium medium, ICraftingPatternDetails api)
	{
		List<ICraftingMedium> details = craftingMethods.get( api );
		if ( details == null )
		{
			details = new ArrayList<ICraftingMedium>();
			details.add( medium );
			craftingMethods.put( api, details );
		}
		else
			details.add( medium );
	}

	static Comparator<ICraftingPatternDetails> comp = new Comparator<ICraftingPatternDetails>(){
		@Override
		public int compare(ICraftingPatternDetails o1,
				ICraftingPatternDetails o2) {
			return o2.getPriority() - o1.getPriority();
		}
	};

	private void updatePatterns()
	{
		HashMap<IAEItemStack, ImmutableList<ICraftingPatternDetails>> oldItems = craftableItems;

		// erase list.
		craftingMethods.clear();
		craftableItems = new HashMap<IAEItemStack, ImmutableList<ICraftingPatternDetails>>();
		emitableItems.clear();

		// update the stuff that was in the list...
		sg.postAlterationOfStoredItems( StorageChannel.ITEMS, oldItems.keySet(), new BaseActionSource() );

		// re-create list..
		for (ICraftingProvider cp : providers)
			cp.provideCrafting( this );

		HashMap<IAEItemStack, Set<ICraftingPatternDetails>> tmpCraft = new HashMap<IAEItemStack, Set<ICraftingPatternDetails>>();

		// new craftables!
		for (ICraftingPatternDetails details : craftingMethods.keySet())
		{
			for (IAEItemStack out : details.getOutputs())
			{
				out = out.copy();
				out.reset();
				out.setCraftable( true );

				Set<ICraftingPatternDetails> methods = tmpCraft.get( out );

				if ( methods == null )
					tmpCraft.put( out, methods = new TreeSet<ICraftingPatternDetails>(comp) );

				methods.add( details );
			}
		}

		// make them immutable
		for (Entry<IAEItemStack, Set<ICraftingPatternDetails>> e : tmpCraft.entrySet())
			craftableItems.put( e.getKey(), ImmutableList.copyOf( e.getValue() ) );

		sg.postAlterationOfStoredItems( StorageChannel.ITEMS, craftableItems.keySet(), new BaseActionSource() );
	}

	@Override
	public void onSplit(IGridStorage destinationStorage)
	{ // nothing!
	}

	@Override
	public void onJoin(IGridStorage sourceStorage)
	{
		// nothing!
	}

	@Override
	public void populateGridStorage(IGridStorage destinationStorage)
	{
		// nothing!
	}

	@Override
	public List<IMEInventoryHandler> getCellArray(StorageChannel channel)
	{
		ArrayList<IMEInventoryHandler> list = new ArrayList<IMEInventoryHandler>( 1 );
		if ( channel == StorageChannel.ITEMS )
			list.add( this );
		return list;
	}

	@Override
	public int getPriority()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public IAEStack extractItems(IAEStack request, Actionable mode, BaseActionSource src)
	{
		return null;
	}

	@Override
	public IItemList getAvailableItems(IItemList out)
	{
		// add craftable items!
		for (IAEItemStack st : craftableItems.keySet())
			out.addCrafting( st );

		for (IAEItemStack st : emitableItems)
			out.addCrafting( st );

		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public AccessRestriction getAccess()
	{
		return AccessRestriction.WRITE;
	}

	@Override
	public boolean isPrioritized(IAEStack input)
	{
		return true;
	}

	@Override
	public IAEStack injectItems(IAEStack input, Actionable type, BaseActionSource src)
	{
		for (CraftingCPUCluster cpu : cpuClusters)
			input = cpu.injectItems( input, type, src );

		return input;
	}

	@Override
	public boolean canAccept(IAEStack input)
	{
		for (CraftingCPUCluster cpu : cpuClusters)
			if ( cpu.canAccept( input ) )
				return true;

		return false;
	}

	@Override
	public ICraftingLink submitJob(ICraftingJob job, ICraftingRequester requestingMachine, ICraftingCPU target, final boolean prioritizePower,
			BaseActionSource src)
	{
		if ( job.isSimulation() )
			return null;

		CraftingCPUCluster cpuCluster = null;

		if ( target instanceof CraftingCPUCluster )
			cpuCluster = (CraftingCPUCluster) target;

		if ( target == null )
		{
			List<CraftingCPUCluster> validCpusClusters = new ArrayList<CraftingCPUCluster>();
			for (CraftingCPUCluster cpu : cpuClusters)
			{
				if ( cpu.isActive() && !cpu.isBusy() && cpu.getAvailableStorage() >= job.getByteTotal() )
				{
					validCpusClusters.add( cpu );
				}
			}

			Collections.sort( validCpusClusters, new Comparator<CraftingCPUCluster>() {

				@Override
				public int compare(CraftingCPUCluster o1, CraftingCPUCluster o2)
				{
					if ( prioritizePower )
					{
						int a = ItemSorters.compareLong( o2.getCoProcessors(), o1.getCoProcessors() );
						if ( a != 0 )
							return a;
						return ItemSorters.compareLong( o2.getAvailableStorage(), o1.getAvailableStorage() );
					}

					int a = ItemSorters.compareLong( o1.getCoProcessors(), o2.getCoProcessors() );
					if ( a != 0 )
						return a;
					return ItemSorters.compareLong( o1.getAvailableStorage(), o2.getAvailableStorage() );
				}

			} );

			if ( !validCpusClusters.isEmpty() )
				cpuCluster = validCpusClusters.get( 0 );
		}

		if ( cpuCluster != null )
		{
			return cpuCluster.submitJob( grid, job, src, requestingMachine );
		}

		return null;
	}

	@Override
	public int getSlot()
	{
		return 0;
	}

	@Override
	public ImmutableCollection<ICraftingPatternDetails> getCraftingFor(IAEItemStack whatToCraft, ICraftingPatternDetails details, int slotIndex, World world)
	{
		ImmutableList<ICraftingPatternDetails> res = craftableItems.get( whatToCraft );

		if ( res == null )
		{
			if ( details != null && details.isCraftable() )
			{
				for (IAEItemStack ais : craftableItems.keySet())
				{
					if ( ais.getItem() == whatToCraft.getItem() && (!ais.getItem().getHasSubtypes() || ais.getItemDamage() == whatToCraft.getItemDamage()) )
					{
						if ( details.isValidItemForSlot( slotIndex, ais.getItemStack(), world ) )
						{
							return craftableItems.get( ais );
						}
					}
				}
			}

			return ImmutableSet.of();
		}

		return res;
	}

	public List<ICraftingMedium> getMediums(ICraftingPatternDetails key)
	{
		List<ICraftingMedium> o = craftingMethods.get( key );

		if ( o == null )
			o = ImmutableList.of();

		return o;
	}

	@Override
	public boolean validForPass(int i)
	{
		return i == 1;
	}

	final public static ExecutorService craftingPool;

	static
	{
		ThreadFactory factory = new ThreadFactory() {

			@Override
			public Thread newThread(Runnable ar)
			{
				return new Thread( ar, "AE Crafting Calculator" );
			}

		};

		craftingPool = Executors.newCachedThreadPool( factory );
	}

	@Override
	public Future<ICraftingJob> beginCraftingJob(World world, IGrid grid, BaseActionSource actionSrc, IAEItemStack slotItem, ICraftingCallback cb)
	{
		if ( world == null || grid == null || actionSrc == null || slotItem == null )
			throw new RuntimeException( "Invalid Crafting Job Request" );

		CraftingJob cj = new CraftingJob( world, grid, actionSrc, slotItem, cb );
		return craftingPool.submit( cj, (ICraftingJob) cj );
	}

	public boolean hasCpu(ICraftingCPU cpu)
	{
		return cpuClusters.contains( cpu );
	}

	@Override
	public boolean isRequesting(IAEItemStack what)
	{
		for (CraftingCPUCluster c : cpuClusters)
			if ( c.isMaking( what ) )
				return true;
		return false;
	}

	@Override
	public boolean canEmitFor(IAEItemStack what)
	{
		return emitableItems.contains( what );
	}

	@Override
	public void setEmitable(IAEItemStack what)
	{
		emitableItems.add( what.copy() );
	}

}
