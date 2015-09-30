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

package appeng.core.features.registries;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridCacheRegistry;
import appeng.core.AELog;


public final class GridCacheRegistry implements IGridCacheRegistry
{
	private final Map<Class<? extends IGridCache>, Class<? extends IGridCache>> caches = new HashMap<Class<? extends IGridCache>, Class<? extends IGridCache>>();

	@Override
	public void registerGridCache( Class<? extends IGridCache> iface, Class<? extends IGridCache> implementation )
	{
		if( iface.isAssignableFrom( implementation ) )
		{
			this.caches.put( iface, implementation );
		}
		else
		{
			throw new IllegalArgumentException( "Invalid setup, grid cache must either be the same class, or an interface that the implementation implements. Gotten: " + iface + " and " + implementation );
		}
	}

	@Override
	public HashMap<Class<? extends IGridCache>, IGridCache> createCacheInstance( IGrid g )
	{
		HashMap<Class<? extends IGridCache>, IGridCache> map = new HashMap<Class<? extends IGridCache>, IGridCache>();

		for( Class<? extends IGridCache> iface : this.caches.keySet() )
		{
			try
			{
				Constructor<? extends IGridCache> c = this.caches.get( iface ).getConstructor( IGrid.class );
				map.put( iface, c.newInstance( g ) );
			}
			catch( NoSuchMethodException e )
			{
				AELog.severe( "Grid Caches must have a constructor with IGrid as the single param." );
				throw new IllegalArgumentException( e );
			}
			catch( InvocationTargetException e )
			{
				AELog.severe( "Grid Caches must have a constructor with IGrid as the single param." );
				throw new IllegalStateException( e );
			}
			catch( InstantiationException e )
			{
				AELog.severe( "Grid Caches must have a constructor with IGrid as the single param." );
				throw new IllegalStateException( e );
			}
			catch( IllegalAccessException e )
			{
				AELog.severe( "Grid Caches must have a constructor with IGrid as the single param." );
				throw new IllegalStateException( e );
			}
		}

		return map;
	}
}
