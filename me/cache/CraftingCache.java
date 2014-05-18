package appeng.me.cache;

import java.util.HashSet;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingCpuChange;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingStorageTile;
import appeng.tile.crafting.TileCraftingTile;

public class CraftingCache implements IGridCache, ICraftingProviderHelper
{

	HashSet<CraftingCPUCluster> cpuClusters = new HashSet();
	HashSet<ICraftingProvider> providers = new HashSet();

	IGrid grid;

	boolean updateList = false;

	public CraftingCache(IGrid g) {
		grid = g;
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

	public void updateCPUClusters(MENetworkCraftingCpuChange c)
	{
		updateList = true;
	}

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
	public void addCraftingOption(ICraftingPatternDetails api)
	{
		// TODO Auto-generated method stub

	}

	private void updatePatterns()
	{
		for (ICraftingProvider cp : providers)
		{
			cp.provideCrafting( this );
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

}
