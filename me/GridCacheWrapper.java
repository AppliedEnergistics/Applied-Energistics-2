package appeng.me;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.me.cache.TickManagerCache;
import appeng.util.Platform;

public class GridCacheWrapper implements IGridCache
{

	final IGridCache myCache;
	final public boolean isTickHandler;

	public long LastFiveTicksTime = 0;
	public long LastFiveRemoveTime = 0;
	public long LastFiveAddNode = 0;

	public GridCacheWrapper(IGridCache gc) {
		myCache = gc;
		isTickHandler = myCache instanceof TickManagerCache;
	}

	@Override
	public void onUpdateTick(IGrid grid)
	{
		long startTime = Platform.nanoTime();

		myCache.onUpdateTick( grid );

		long estimatedTime = Platform.nanoTime() - startTime;

		if ( isTickHandler ) // remove the ticking of tickables from the
								// equation.
			estimatedTime -= ((TickManagerCache) myCache).getInnerTime();

		LastFiveTicksTime = ((LastFiveTicksTime * 4) / 5) + estimatedTime;
	}

	@Override
	public void removeNode(IGrid grid, IGridNode gridNode, IGridHost machine)
	{
		long startTime = Platform.nanoTime();

		myCache.removeNode( grid, gridNode, machine );

		long estimatedTime = Platform.nanoTime() - startTime;
		LastFiveRemoveTime = ((LastFiveRemoveTime * 4) / 5) + estimatedTime;
	}

	@Override
	public void addNode(IGrid grid, IGridNode gridNode, IGridHost machine)
	{
		long startTime = Platform.nanoTime();

		myCache.addNode( grid, gridNode, machine );

		long estimatedTime = Platform.nanoTime() - startTime;
		LastFiveAddNode = ((LastFiveAddNode * 4) / 5) + estimatedTime;
	}

	public String getName()
	{
		return myCache.getClass().getName();
	}

	@Override
	public void onSplit(IGridStorage storageB)
	{
		myCache.onSplit( storageB );
	}

	@Override
	public void onJoin(IGridStorage storageB)
	{
		myCache.onJoin( storageB );
	}

	@Override
	public void populateGridStorage(IGridStorage storage)
	{
		myCache.populateGridStorage( storage );
	}

}
