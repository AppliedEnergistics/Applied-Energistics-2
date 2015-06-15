/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.core.worlddata;


import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import appeng.core.AELog;
import appeng.me.GridStorage;
import appeng.me.GridStorageSearch;


/**
 * @author thatsIch
 * @version rv3 - 30.05.2015
 * @since rv3 30.05.2015
 */
final class StorageData implements IWorldGridStorageData, IOnWorldStartable, IOnWorldStoppable
{
	private static final String LAST_GRID_STORAGE_CATEGORY = "Counters";
	private static final String LAST_GRID_STORAGE_KEY = "lastGridStorage";
	private static final int LAST_GRID_STORAGE_DEFAULT = 0;

	private static final String GRID_STORAGE_CATEGORY = "gridstorage";

	private final Map<GridStorageSearch, WeakReference<GridStorageSearch>> loadedStorage = new WeakHashMap<GridStorageSearch, WeakReference<GridStorageSearch>>( 10 );
	private final Configuration config;

	private long lastGridStorage;

	public StorageData( @Nonnull final File settingsFile, @Nonnull final String version )
	{
		Preconditions.checkNotNull( settingsFile );
		Preconditions.checkNotNull( version );
		Preconditions.checkArgument( !version.isEmpty() );

		this.config = new Configuration( settingsFile, version );
	}

	/**
	 * lazy loading, can load any id, even ones that don't exist anymore.
	 *
	 * @param storageID ID of grid storage
	 *
	 * @return corresponding grid storage
	 */
	@Nullable
	@Override
	public GridStorage getGridStorage( long storageID )
	{
		GridStorageSearch gss = new GridStorageSearch( storageID );
		WeakReference<GridStorageSearch> result = this.loadedStorage.get( gss );

		if( result == null || result.get() == null )
		{
			String id = String.valueOf( storageID );
			String data = this.config.get( "gridstorage", id, "" ).getString();
			GridStorage thisStorage = new GridStorage( data, storageID, gss );
			gss.gridStorage = new WeakReference<GridStorage>( thisStorage );
			this.loadedStorage.put( gss, new WeakReference<GridStorageSearch>( gss ) );
			return thisStorage;
		}

		return result.get().gridStorage.get();
	}

	/**
	 * create a new storage
	 */
	@Nonnull
	@Override
	public GridStorage getNewGridStorage()
	{
		long storageID = this.nextGridStorage();
		GridStorageSearch gss = new GridStorageSearch( storageID );
		GridStorage newStorage = new GridStorage( storageID, gss );
		gss.gridStorage = new WeakReference<GridStorage>( newStorage );
		this.loadedStorage.put( gss, new WeakReference<GridStorageSearch>( gss ) );

		return newStorage;
	}

	@Override
	public long nextGridStorage()
	{
		long r = this.lastGridStorage;
		this.lastGridStorage++;
		this.config.get( "Counters", "lastGridStorage", this.lastGridStorage ).set( Long.toString( this.lastGridStorage ) );
		return r;
	}

	@Override
	public void destroyGridStorage( long id )
	{
		String stringID = String.valueOf( id );
		this.config.getCategory( "gridstorage" ).remove( stringID );
	}

	@Override
	public int getNextOrderedValue( String name )
	{
		Property p = this.config.get( "orderedValues", name, 0 );
		int myValue = p.getInt();
		p.set( myValue + 1 );
		return myValue;
	}

	@Override
	public void onWorldStart()
	{
		final String lastString = this.config.get( LAST_GRID_STORAGE_CATEGORY, LAST_GRID_STORAGE_KEY, LAST_GRID_STORAGE_DEFAULT ).getString();

		try
		{
			this.lastGridStorage = Long.parseLong( lastString );
		}
		catch( NumberFormatException err )
		{
			AELog.warning( "The config contained a value which was not represented as a Long: %s", lastString );

			this.lastGridStorage = 0;
		}
	}

	@Override
	public void onWorldStop()
	{
		// populate new data
		for( GridStorageSearch gs : this.loadedStorage.keySet() )
		{
			GridStorage thisStorage = gs.gridStorage.get();
			if( thisStorage != null && thisStorage.getGrid() != null && !thisStorage.getGrid().isEmpty() )
			{
				String value = thisStorage.getValue();
				this.config.get( GRID_STORAGE_CATEGORY, String.valueOf( thisStorage.getID() ), value ).set( value );
			}
		}
	}
}
