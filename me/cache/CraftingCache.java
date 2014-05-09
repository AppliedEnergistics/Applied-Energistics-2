package appeng.me.cache;

import java.util.HashSet;

import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.me.cluster.implementations.CraftingCPUCluster;

public class CraftingCache implements IGridCache
{

	HashSet<CraftingCPUCluster> cpuClusters = new HashSet();
	HashSet<ICraftingProvider> providers = new HashSet();

	@Override
	public void onUpdateTick()
	{

	}

	@Override
	public void removeNode(IGridNode gridNode, IGridHost machine)
	{
		if ( machine instanceof ICraftingProvider )
			providers.remove( machine );

		updatePatterns();
	}

	@Override
	public void addNode(IGridNode gridNode, IGridHost machine)
	{
		if ( machine instanceof ICraftingProvider )
			providers.add( (ICraftingProvider) machine );

		updatePatterns();
	}

	private void updatePatterns()
	{
		// TODO Auto-generated method stub

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
