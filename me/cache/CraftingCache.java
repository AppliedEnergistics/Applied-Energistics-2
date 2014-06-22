package appeng.me.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
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
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingStorageTile;
import appeng.tile.crafting.TileCraftingTile;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class CraftingCache implements IGridCache, ICraftingProviderHelper, ICellProvider, IMEInventoryHandler
{

	HashSet<CraftingCPUCluster> cpuClusters = new HashSet();
	HashSet<ICraftingProvider> providers = new HashSet();
	IGrid grid;

	HashMap<ICraftingPatternDetails, List<ICraftingMedium>> craftingMethods = new HashMap();
	HashMap<IAEItemStack, Set<ICraftingPatternDetails>> craftableItems = new HashMap();

	boolean updateList = false;

	public CraftingCache(IGrid g) {
		grid = g;
	}

	@MENetworkEventSubscribe
	public void afterCacheConstruction(MENetworkPostCacheConstruction cc)
	{
		IStorageGrid sg = grid.getCache( IStorageGrid.class );
		sg.registerCellProvider( this );
	}

	@Override
	public void onUpdateTick()
	{
		if ( updateList )
		{
			updateList = false;
			updateCPUClusters();
		}

		for (CraftingCPUCluster cpu : cpuClusters)
			cpu.updateCraftingLogic( grid, this );
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
			CraftingCPUCluster clust = (CraftingCPUCluster) tile.getCluster();
			if ( clust != null )
				cpuClusters.add( clust );
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

	private void updatePatterns()
	{
		IStorageGrid sg = grid.getCache( IStorageGrid.class );

		// update the stuff that was in the list...
		for (IAEItemStack out : craftableItems.keySet())
		{
			out.reset();
			sg.postAlterationOfStoredItems( StorageChannel.ITEMS, out, new BaseActionSource() );
		}

		// erase list.
		craftingMethods.clear();
		craftableItems.clear();

		// re-create list..
		for (ICraftingProvider cp : providers)
			cp.provideCrafting( this );

		// new craftables!
		for (ICraftingPatternDetails details : craftingMethods.keySet())
			for (IAEItemStack out : details.getOutputs())
			{
				out = out.copy();
				out.reset();
				out.setCraftable( true );

				Set<ICraftingPatternDetails> methods = craftableItems.get( out );

				if ( methods == null )
					craftableItems.put( out, methods = new TreeSet() );

				methods.add( details );
				sg.postAlterationOfStoredItems( StorageChannel.ITEMS, out, new BaseActionSource() );
			}
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
		// for (ICraftingPatternDetails details : craftingMethods.keySet())
		for (IAEItemStack st : craftableItems.keySet())
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
			if ( cpu.canAccept( (IAEItemStack) input ) )
				return true;

		return false;
	}

	public boolean submitJob(CraftingJob job, CraftingCPUCluster target, BaseActionSource src)
	{
		if ( job.isSimulation() )
			return false;

		if ( target == null )
		{
			// TODO real stuff...
			for (CraftingCPUCluster cpu : cpuClusters)
			{
				if ( !cpu.isBusy() )
				{
					target = cpu;
					break;
				}
			}
		}

		if ( target != null && target.submitJob( grid, job, src ) )
			return true;

		return false;
	}

	@Override
	public int getSlot()
	{
		return 0;
	}

	public Set<ICraftingPatternDetails> getCraftingFor(IAEItemStack what)
	{
		Set<ICraftingPatternDetails> res = craftableItems.get( what );
		if ( res == null )
			return ImmutableSet.of();
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

}
