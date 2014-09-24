package appeng.core.features.registries;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridCacheRegistry;
import appeng.core.AELog;

public class GridCacheRegistry implements IGridCacheRegistry
{

	final private HashMap<Class<? extends IGridCache>, Class<? extends IGridCache>> caches = new HashMap();

	@Override
	public void registerGridCache(Class<? extends IGridCache> iface, Class<? extends IGridCache> implementation)
	{
		if ( iface.isAssignableFrom( implementation ) )
			caches.put( iface, implementation );
		else
			throw new RuntimeException( "Invalid setup, grid cache must either be the same class, or an interface that the implementation implements" );
	}

	@Override
	public HashMap<Class<? extends IGridCache>, IGridCache> createCacheInstance(IGrid g)
	{
		HashMap<Class<? extends IGridCache>, IGridCache> map = new HashMap();

		for (Class<? extends IGridCache> iface : caches.keySet())
		{
			try
			{
				Constructor<? extends IGridCache> c = caches.get( iface ).getConstructor( IGrid.class );
				map.put( iface, c.newInstance( g ) );
			}
			catch (Throwable e)
			{
				AELog.severe( "Grid Caches must have a constructor with IGrid as the single param." );
				throw new RuntimeException( e );
			}
		}

		return map;
	}
}
