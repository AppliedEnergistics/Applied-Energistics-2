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


import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.base.Preconditions;

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

	private final Map<GridStorageSearch, WeakReference<GridStorageSearch>> loadedStorage = new WeakHashMap<>( 10 );
	private final CommentedFileConfig config;

	private long lastGridStorage;

	public StorageData( @Nonnull final CommentedFileConfig settingsFile )
	{
		Preconditions.checkNotNull( settingsFile );

		this.config = settingsFile;
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
	public GridStorage getGridStorage( final long storageID )
	{
		return null;
// FIXME		final GridStorageSearch gss = new GridStorageSearch( storageID );
// FIXME		final WeakReference<GridStorageSearch> result = this.loadedStorage.get( gss );
// FIXME
// FIXME		if( result == null || result.get() == null )
// FIXME		{
// FIXME			final String id = String.valueOf( storageID );
// FIXME			final String data = this.config.get( "gridstorage", id, "" ).getString();
// FIXME			final GridStorage thisStorage = new GridStorage( data, storageID, gss );
// FIXME			gss.setGridStorage( new WeakReference<>( thisStorage ) );
// FIXME			this.loadedStorage.put( gss, new WeakReference<>( gss ) );
// FIXME			return thisStorage;
// FIXME		}
// FIXME
// FIXME		return result.get().getGridStorage().get();
	}

	/**
	 * create a new storage
	 */
	@Nonnull
	@Override
	public GridStorage getNewGridStorage()
	{
		final long storageID = this.nextGridStorage();
		final GridStorageSearch gss = new GridStorageSearch( storageID );
		final GridStorage newStorage = new GridStorage( storageID, gss );
		gss.setGridStorage( new WeakReference<>( newStorage ) );
		this.loadedStorage.put( gss, new WeakReference<>( gss ) );

		return newStorage;
	}

	@Override
	public long nextGridStorage()
	{
// FIXME		final long r = this.lastGridStorage;
// FIXME		this.lastGridStorage++;
// FIXME		this.config.get( "Counters", "lastGridStorage", this.lastGridStorage ).set( Long.toString( this.lastGridStorage ) );
// FIXME		return r;
		return 0;
	}

	@Override
	public void destroyGridStorage( final long id )
	{
// FIXME		final String stringID = String.valueOf( id );
// FIXME		this.config.getCategory( "gridstorage" ).remove( stringID );
	}

	@Override
	public int getNextOrderedValue( final String name )
	{
// FIXME		final Property p = this.config.get( "orderedValues", name, 0 );
// FIXME		final int myValue = p.getInt();
// FIXME		p.set( myValue + 1 );
// FIXME		return myValue;
		return 0;
	}

	@Override
	public void onWorldStart()
	{
// FIXME		final String lastString = this.config.get( LAST_GRID_STORAGE_CATEGORY, LAST_GRID_STORAGE_KEY, LAST_GRID_STORAGE_DEFAULT ).getString();
// FIXME
// FIXME		try
// FIXME		{
// FIXME			this.lastGridStorage = Long.parseLong( lastString );
// FIXME		}
// FIXME		catch( final NumberFormatException err )
// FIXME		{
// FIXME			AELog.warn( "The config contained a value which was not represented as a Long: %s", lastString );
// FIXME
// FIXME			this.lastGridStorage = 0;
// FIXME		}
	}

	@Override
	public void onWorldStop()
	{
// FIXME		// populate new data
// FIXME		for( final GridStorageSearch gs : this.loadedStorage.keySet() )
// FIXME		{
// FIXME			final GridStorage thisStorage = gs.getGridStorage().get();
// FIXME			if( thisStorage != null && thisStorage.getGrid() != null && !thisStorage.getGrid().isEmpty() )
// FIXME			{
// FIXME				final String value = thisStorage.getValue();
// FIXME				this.config.get( GRID_STORAGE_CATEGORY, String.valueOf( thisStorage.getID() ), value ).set( value );
// FIXME			}
// FIXME		}
// FIXME
// FIXME		this.config.save();
	}
}
