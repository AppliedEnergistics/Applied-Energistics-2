package appeng.me;

import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;

public class GridCacheWrapper implements IGridCache
{

	final IGridCache myCache;
	final String name;

	public GridCacheWrapper(final IGridCache gc) {
		myCache = gc;
		name = myCache.getClass().getName();
	}

	@Override
	public void onUpdateTick()
	{
		myCache.onUpdateTick();
	}

	@Override
	public void removeNode(final IGridNode gridNode, final IGridHost machine)
	{
		myCache.removeNode( gridNode, machine );
	}

	@Override
	public void addNode(final IGridNode gridNode, final IGridHost machine)
	{
		myCache.addNode( gridNode, machine );
	}

	public String getName()
	{
		return name;
	}

	@Override
	public void onSplit(final IGridStorage storageB)
	{
		myCache.onSplit( storageB );
	}

	@Override
	public void onJoin(final IGridStorage storageB)
	{
		myCache.onJoin( storageB );
	}

	@Override
	public void populateGridStorage(final IGridStorage storage)
	{
		myCache.populateGridStorage( storage );
	}

}
