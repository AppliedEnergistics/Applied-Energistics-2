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

package appeng.core;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import com.google.common.base.Optional;
import com.mojang.authlib.GameProfile;

import appeng.api.util.WorldCoord;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketNewStorageDimension;
import appeng.hooks.TickHandler;
import appeng.hooks.TickHandler.PlayerColor;
import appeng.me.GridStorage;
import appeng.me.GridStorageSearch;
import appeng.services.CompassService;


public class WorldSettings extends Configuration
{
	private static final String SPAWNDATA_FOLDER = "spawndata";
	private static final String COMPASS_Folder = "compass";

	private static WorldSettings instance;
	private final List<Integer> storageCellDims = new ArrayList<Integer>();
	private final File aeFolder;
	private final CompassService compass;
	private final PlayerMappings mappings;
	private final WeakHashMap<GridStorageSearch, WeakReference<GridStorageSearch>> loadedStorage = new WeakHashMap<GridStorageSearch, WeakReference<GridStorageSearch>>();
	private long lastGridStorage;
	private int lastPlayer;

	public WorldSettings( File aeFolder )
	{
		super( new File( aeFolder.getPath() + File.separatorChar + "settings.cfg" ) );
		this.aeFolder = aeFolder;
		this.compass = new CompassService( aeFolder );

		for ( int dimID : get( "DimensionManager", "StorageCells", new int[0] ).getIntList() )
		{
			storageCellDims.add( dimID );
			DimensionManager.registerDimension( dimID, AEConfig.instance.storageProviderID );
		}

		try
		{
			lastGridStorage = Long.parseLong( get( "Counters", "lastGridStorage", 0 ).getString() );
			lastPlayer = get( "Counters", "lastPlayer", 0 ).getInt();
		}
		catch ( NumberFormatException err )
		{
			lastGridStorage = 0;
			lastPlayer = 0;
		}

		final ConfigCategory playerList = this.getCategory( "players" );
		this.mappings = new PlayerMappings( playerList, AELog.instance );
	}

	public static WorldSettings getInstance()
	{
		if ( instance == null )
		{
			File world = DimensionManager.getCurrentSaveRootDirectory();

			File aeBaseFolder = new File( world.getPath() + File.separatorChar + "AE2" );

			if ( !aeBaseFolder.isDirectory() && !aeBaseFolder.mkdir() )
			{
				throw new RuntimeException( "Failed to create " + aeBaseFolder.getAbsolutePath() );
			}

			File compass = new File( aeBaseFolder, COMPASS_Folder );
			if ( !compass.isDirectory() && !compass.mkdir() )
			{
				throw new RuntimeException( "Failed to create " + compass.getAbsolutePath() );
			}

			File spawnData = new File( aeBaseFolder, SPAWNDATA_FOLDER );
			if ( !spawnData.isDirectory() && !spawnData.mkdir() )
			{
				throw new RuntimeException( "Failed to create " + spawnData.getAbsolutePath() );
			}

			instance = new WorldSettings( aeBaseFolder );
		}

		return instance;
	}

	public Collection<NBTTagCompound> getNearByMeteorites( int dim, int chunkX, int chunkZ )
	{
		LinkedList<NBTTagCompound> ll = new LinkedList<NBTTagCompound>();

		synchronized ( WorldSettings.class )
		{
			for ( int x = -1; x <= 1; x++ )
			{
				for ( int z = -1; z <= 1; z++ )
				{
					int cx = x + ( chunkX >> 4 );
					int cz = z + ( chunkZ >> 4 );

					NBTTagCompound data = loadSpawnData( dim, cx << 4, cz << 4 );

					if ( data != null )
					{
						// edit.
						int size = data.getInteger( "num" );
						for ( int s = 0; s < size; s++ )
							ll.add( data.getCompoundTag( "" + s ) );
					}
				}
			}
		}

		return ll;
	}

	NBTTagCompound loadSpawnData( int dim, int chunkX, int chunkZ )
	{
		if ( !Thread.holdsLock( WorldSettings.class ) )
			throw new RuntimeException( "Invalid Request" );

		NBTTagCompound data = null;
		File file = new File( aeFolder, SPAWNDATA_FOLDER + File.separatorChar + dim + "_" + ( chunkX >> 4 ) + "_" + ( chunkZ >> 4 ) + ".dat" );

		if ( file.isFile() )
		{
			FileInputStream fileInputStream = null;

			try
			{
				fileInputStream = new FileInputStream( file );
				data = CompressedStreamTools.readCompressed( fileInputStream );
			}
			catch ( Throwable e )
			{
				data = new NBTTagCompound();
				AELog.error( e );
			}
			finally
			{
				if ( fileInputStream != null )
				{
					try
					{
						fileInputStream.close();
					}
					catch ( IOException e )
					{
						AELog.error( e );
					}
				}
			}
		}
		else
		{
			data = new NBTTagCompound();
		}

		return data;
	}

	public boolean hasGenerated( int dim, int chunkX, int chunkZ )
	{
		synchronized ( WorldSettings.class )
		{
			NBTTagCompound data = loadSpawnData( dim, chunkX, chunkZ );
			return data.getBoolean( chunkX + "," + chunkZ );
		}
	}

	public void setGenerated( int dim, int chunkX, int chunkZ )
	{
		synchronized ( WorldSettings.class )
		{
			NBTTagCompound data = loadSpawnData( dim, chunkX, chunkZ );

			// edit.
			data.setBoolean( chunkX + "," + chunkZ, true );

			writeSpawnData( dim, chunkX, chunkZ, data );
		}
	}

	void writeSpawnData( int dim, int chunkX, int chunkZ, NBTTagCompound data )
	{
		if ( !Thread.holdsLock( WorldSettings.class ) )
			throw new RuntimeException( "Invalid Request" );

		File file = new File( aeFolder, SPAWNDATA_FOLDER + File.separatorChar + dim + "_" + ( chunkX >> 4 ) + "_" + ( chunkZ >> 4 ) + ".dat" );
		FileOutputStream fileOutputStream = null;

		try
		{
			fileOutputStream = new FileOutputStream( file );
			CompressedStreamTools.writeCompressed( data, fileOutputStream );
		}
		catch ( Throwable e )
		{
			AELog.error( e );
		}
		finally
		{
			if ( fileOutputStream != null )
			{
				try
				{
					fileOutputStream.close();
				}
				catch ( IOException e )
				{
					AELog.error( e );
				}
			}
		}
	}

	public boolean addNearByMeteorites( int dim, int chunkX, int chunkZ, NBTTagCompound newData )
	{
		synchronized ( WorldSettings.class )
		{
			NBTTagCompound data = loadSpawnData( dim, chunkX, chunkZ );

			// edit.
			int size = data.getInteger( "num" );
			data.setTag( "" + size, newData );
			data.setInteger( "num", size + 1 );

			writeSpawnData( dim, chunkX, chunkZ, data );

			return true;
		}
	}

	public void shutdown()
	{
		save();

		for ( Integer dimID : storageCellDims )
			DimensionManager.unregisterDimension( dimID );

		storageCellDims.clear();

		compass.kill();
		instance = null;
	}

	@Override
	public void save()
	{
		// populate new data
		for ( GridStorageSearch gs : loadedStorage.keySet() )
		{
			GridStorage thisStorage = gs.gridStorage.get();
			if ( thisStorage != null && thisStorage.getGrid() != null && !thisStorage.getGrid().isEmpty() )
			{
				String value = thisStorage.getValue();
				get( "gridstorage", "" + thisStorage.getID(), value ).set( value );
			}
		}

		// save to files
		if ( hasChanged() )
			super.save();
	}

	public void addStorageCellDim( int newDim )
	{
		storageCellDims.add( newDim );
		DimensionManager.registerDimension( newDim, AEConfig.instance.storageProviderID );

		NetworkHandler.instance.sendToAll( new PacketNewStorageDimension( newDim ) );

		String[] values = new String[storageCellDims.size()];

		for ( int x = 0; x < values.length; x++ )
			values[x] = "" + storageCellDims.get( x );

		get( "DimensionManager", "StorageCells", new int[0] ).set( values );
		save();
	}

	public CompassService getCompass()
	{
		return compass;
	}

	public void sendToPlayer( NetworkManager manager, EntityPlayerMP player )
	{
		if ( manager != null )
		{
			for ( int newDim : get( "DimensionManager", "StorageCells", new int[0] ).getIntList() )
			{
				manager.scheduleOutboundPacket( ( new PacketNewStorageDimension( newDim ) ).getProxy() );
			}
		}
		else
		{
			for ( PlayerColor pc : TickHandler.instance.getPlayerColors().values() )
				NetworkHandler.instance.sendToAll( pc.getPacket() );
		}
	}

	public void init()
	{
		save();
	}

	public WorldCoord getStoredSize( int dim )
	{
		int x = get( "StorageCell" + dim, "scaleX", 0 ).getInt();
		int y = get( "StorageCell" + dim, "scaleY", 0 ).getInt();
		int z = get( "StorageCell" + dim, "scaleZ", 0 ).getInt();
		return new WorldCoord( x, y, z );
	}

	public void setStoredSize( int dim, int targetX, int targetY, int targetZ )
	{
		get( "StorageCell" + dim, "scaleX", 0 ).set( targetX );
		get( "StorageCell" + dim, "scaleY", 0 ).set( targetY );
		get( "StorageCell" + dim, "scaleZ", 0 ).set( targetZ );
		save();
	}

	/**
	 * lazy loading, can load any id, even ones that don't exist anymore.
	 *
	 * @param storageID ID of grid storage
	 *
	 * @return corresponding grid storage
	 */
	public GridStorage getGridStorage( long storageID )
	{
		GridStorageSearch gss = new GridStorageSearch( storageID );
		WeakReference<GridStorageSearch> result = loadedStorage.get( gss );

		if ( result == null || result.get() == null )
		{
			String Data = get( "gridstorage", "" + storageID, "" ).getString();
			GridStorage thisStorage = new GridStorage( Data, storageID, gss );
			gss.gridStorage = new WeakReference<GridStorage>( thisStorage );
			loadedStorage.put( gss, new WeakReference<GridStorageSearch>( gss ) );
			return thisStorage;
		}
		return result.get().gridStorage.get();
	}

	/**
	 * create a new storage
	 */
	public GridStorage getNewGridStorage()
	{
		long storageID = nextGridStorage();
		GridStorageSearch gss = new GridStorageSearch( storageID );
		GridStorage newStorage = new GridStorage( storageID, gss );
		gss.gridStorage = new WeakReference<GridStorage>( newStorage );
		loadedStorage.put( gss, new WeakReference<GridStorageSearch>( gss ) );
		return newStorage;
	}

	private long nextGridStorage()
	{
		long r = lastGridStorage++;
		get( "Counters", "lastGridStorage", lastGridStorage ).set( Long.toString( lastGridStorage ) );
		return r;
	}

	public void destroyGridStorage( long id )
	{
		this.getCategory( "gridstorage" ).remove( "" + id );
	}

	public int getNextOrderedValue( String name )
	{
		Property p = this.get( "orderedValues", name, 0 );
		int myValue = p.getInt();
		p.set( myValue + 1 );
		return myValue;
	}

	public int getPlayerID( GameProfile profile )
	{
		ConfigCategory playerList = this.getCategory( "players" );

		if ( playerList == null || profile == null || !profile.isComplete() )
			return -1;

		String uuid = profile.getId().toString();

		Property prop = playerList.get( uuid );
		if ( prop != null && prop.isIntValue() )
			return prop.getInt();
		else
		{
			playerList.put( uuid, prop = new Property( uuid, "" + nextPlayer(), Property.Type.INTEGER ) );
			this.mappings.put( prop.getInt(), profile.getId() ); // add to reverse map
			save();
			return prop.getInt();
		}
	}

	private long nextPlayer()
	{
		long r = lastPlayer++;
		get( "Counters", "lastPlayer", lastPlayer ).set( lastPlayer );
		return r;
	}

	public EntityPlayer getPlayerFromID( int playerID )
	{
		Optional<UUID> maybe = this.mappings.get( playerID );

		if ( maybe.isPresent() )
		{
			final UUID uuid = maybe.get();
			for ( EntityPlayer player : CommonHelper.proxy.getPlayers() )
			{
				if ( player.getUniqueID().equals( uuid ) )
					return player;
			}
		}

		return null;
	}
}
