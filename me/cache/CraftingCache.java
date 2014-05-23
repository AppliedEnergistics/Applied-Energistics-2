package appeng.me.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingStorageTile;
import appeng.tile.crafting.TileCraftingTile;

public class CraftingCache implements IGridCache, ICraftingProviderHelper, ICellProvider, IMEInventoryHandler
{

	HashSet<CraftingCPUCluster> cpuClusters = new HashSet();
	HashSet<ICraftingProvider> providers = new HashSet();
	IGrid grid;

	HashMap<ICraftingPatternDetails, List<ICraftingMedium>> craftingMethods = new HashMap();

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
			cpuClusters.add( (CraftingCPUCluster) tile.getCluster() );
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
		for (ICraftingPatternDetails details : craftingMethods.keySet())
			for (IAEItemStack out : details.getOutputs())
			{
				out.reset();
				sg.postAlterationOfStoredItems( StorageChannel.ITEMS, out, new BaseActionSource() );
			}

		// erase list.
		craftingMethods.clear();

		// re-create list..
		for (ICraftingProvider cp : providers)
			cp.provideCrafting( this );

		// new craftables!
		for (ICraftingPatternDetails details : craftingMethods.keySet())
			for (IAEItemStack out : details.getOutputs())
			{
				out.reset();
				out.setCraftable( true );
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
		for (ICraftingPatternDetails details : craftingMethods.keySet())
			for (IAEItemStack st : details.getOutputs())
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

	@Override
	public int getSlot()
	{
		return 0;
	}

}
