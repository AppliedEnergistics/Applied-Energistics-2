package appeng.core;

import io.netty.util.concurrent.GenericFutureListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
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
import appeng.api.util.WorldCoord;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketNewStorageDimension;
import appeng.hooks.TickHandler;
import appeng.hooks.TickHandler.PlayerColor;
import appeng.me.GridStorage;
import appeng.me.GridStorageSearch;
import appeng.services.CompassService;

import com.mojang.authlib.GameProfile;

public class WorldSettings extends Configuration
{

	private static WorldSettings instance;

	long lastGridStorage = 0;
	int lastPlayer = 0;

	private CompassService compass;

	File AEFolder;

	public WorldSettings(File aeFolder)
	{
		super( new File( aeFolder.getPath() + File.separatorChar + "settings.cfg" ) );
		AEFolder = aeFolder;

		compass = new CompassService( AEFolder );

		for (int dimID : get( "DimensionManager", "StorageCells", new int[0] ).getIntList())
		{
			storageCellDims.add( dimID );
			DimensionManager.registerDimension( dimID, AEConfig.instance.storageProviderID );
		}

		try
		{
			lastGridStorage = Long.parseLong( get( "Counters", "lastGridStorage", 0 ).getString() );
			lastPlayer = get( "Counters", "lastPlayer", 0 ).getInt();
		}
		catch (NumberFormatException err)
		{
			lastGridStorage = 0;
			lastPlayer = 0;
		}
	}

	NBTTagCompound loadSpawnData(int dim, int chunkX, int chunkZ)
	{
		if ( !Thread.holdsLock( WorldSettings.class ) )
			throw new RuntimeException( "Invalid Request" );

		File f = new File( AEFolder, "spawndata" + File.separatorChar + dim + "_" + (chunkX >> 4) + "_" + (chunkZ >> 4) + ".dat" );

		if ( f.isFile() && f.exists() )
		{
			// open
			FileInputStream fis;
			try
			{
				fis = new FileInputStream( f );

				NBTTagCompound data = null;

				try
				{
					data = CompressedStreamTools.readCompressed( fis );
				}
				catch (Throwable e)
				{
					data = new NBTTagCompound();
					AELog.error( e );
				}

				fis.close();

				return data;
			}
			catch (Throwable e)
			{
				AELog.error( e );
			}

		}

		return new NBTTagCompound();
	}

	void writeSpawnData(int dim, int chunkX, int chunkZ, NBTTagCompound data)
	{
		if ( !Thread.holdsLock( WorldSettings.class ) )
			throw new RuntimeException( "Invalid Request" );

		File f = new File( AEFolder, "spawndata" + File.separatorChar + dim + "_" + (chunkX >> 4) + "_" + (chunkZ >> 4) + ".dat" );

		try
		{
			// save
			FileOutputStream fos = new FileOutputStream( f );

			try
			{
				CompressedStreamTools.writeCompressed( data, fos );
			}
			catch (Throwable e)
			{
				AELog.error( e );
			}

			fos.close();
		}
		catch (Throwable e)
		{
			AELog.error( e );
		}
	}

	public Collection<NBTTagCompound> getNearByMetetorites(int dim, int chunkX, int chunkZ)
	{
		LinkedList<NBTTagCompound> ll = new LinkedList<NBTTagCompound>();

		synchronized (WorldSettings.class)
		{
			for (int x = -1; x <= 1; x++)
			{
				for (int z = -1; z <= 1; z++)
				{
					int cx = x + (chunkX >> 4);
					int cz = z + (chunkZ >> 4);

					NBTTagCompound data = loadSpawnData( dim, cx << 4, cz << 4 );

					if ( data != null )
					{
						// edit.
						int size = data.getInteger( "num" );
						for (int s = 0; s < size; s++)
							ll.add( data.getCompoundTag( "" + s ) );
					}
				}
			}
		}

		return ll;
	}

	public boolean hasGenerated(int dim, int chunkX, int chunkZ)
	{
		synchronized (WorldSettings.class)
		{
			NBTTagCompound data = loadSpawnData( dim, chunkX, chunkZ );
			return data.getBoolean( chunkX + "," + chunkZ );
		}
	}

	public void setGenerated(int dim, int chunkX, int chunkZ)
	{
		synchronized (WorldSettings.class)
		{
			NBTTagCompound data = loadSpawnData( dim, chunkX, chunkZ );

			// edit.
			data.setBoolean( chunkX + "," + chunkZ, true );

			writeSpawnData( dim, chunkX, chunkZ, data );
		}
	}

	public boolean addNearByMetetorites(int dim, int chunkX, int chunkZ, NBTTagCompound newData)
	{
		synchronized (WorldSettings.class)
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

		for (Integer dimID : storageCellDims)
			DimensionManager.unregisterDimension( dimID );

		storageCellDims.clear();

		compass.kill();
		instance = null;
	}

	List<Integer> storageCellDims = new ArrayList();
	HashMap<Integer, UUID> idToUUID;

	public void addStorageCellDim(int newDim)
	{
		storageCellDims.add( newDim );
		DimensionManager.registerDimension( newDim, AEConfig.instance.storageProviderID );

		try
		{
			NetworkHandler.instance.sendToAll( new PacketNewStorageDimension( newDim ) );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		String[] values = new String[storageCellDims.size()];

		for (int x = 0; x < values.length; x++)
			values[x] = "" + storageCellDims.get( x );

		get( "DimensionManager", "StorageCells", new int[0] ).set( values );
		save();
	}

	public CompassService getCompass()
	{
		return compass;
	}

	public static WorldSettings getInstance()
	{
		if ( instance == null )
		{
			File world = DimensionManager.getCurrentSaveRootDirectory();

			File aeBaseFolder = new File( world.getPath() + File.separatorChar + "AE2" );

			if ( !aeBaseFolder.exists() || !aeBaseFolder.isDirectory() )
				if ( !aeBaseFolder.mkdir() || !aeBaseFolder.exists() )
				{
					throw new RuntimeException( "Failed to create " + aeBaseFolder.getAbsolutePath() );
				}

			File compass = new File( aeBaseFolder, "compass" );
			if ( !compass.exists() || !compass.isDirectory() )
				if ( !compass.mkdir() || !compass.exists() )
				{
					throw new RuntimeException( "Failed to create " + compass.getAbsolutePath() );
				}

			File spawnData = new File( aeBaseFolder, "spawndata" );
			if ( !spawnData.exists() || !spawnData.isDirectory() )
				if ( !spawnData.mkdir() || !spawnData.exists() )
				{
					throw new RuntimeException( "Failed to create " + spawnData.getAbsolutePath() );
				}

			instance = new WorldSettings( aeBaseFolder );
		}

		return instance;
	}

	public void sendToPlayer(NetworkManager manager, EntityPlayerMP player)
	{
		if ( manager != null )
		{
			for (int newDim : get( "DimensionManager", "StorageCells", new int[0] ).getIntList())
			{
				try
				{
					manager.scheduleOutboundPacket( (new PacketNewStorageDimension( newDim )).getProxy(), new GenericFutureListener[0] );
				}
				catch (IOException e)
				{
					AELog.error( e );
				}
			}
		}
		else
		{
			for (PlayerColor pc : TickHandler.instance.getPlayerColors().values())
				NetworkHandler.instance.sendToAll( pc.getPacket() );
		}
	}

	public void init()
	{
		save();
	}

	private WeakHashMap<GridStorageSearch, WeakReference<GridStorageSearch>> loadedStorage = new WeakHashMap();

	public WorldCoord getStoredSize(int dim)
	{
		int x = get( "StorageCell" + dim, "scaleX", 0 ).getInt();
		int y = get( "StorageCell" + dim, "scaleY", 0 ).getInt();
		int z = get( "StorageCell" + dim, "scaleZ", 0 ).getInt();
		return new WorldCoord( x, y, z );
	}

	public void setStoredSize(int dim, int targetX, int targetY, int targetZ)
	{
		get( "StorageCell" + dim, "scaleX", 0 ).set( targetX );
		get( "StorageCell" + dim, "scaleY", 0 ).set( targetY );
		get( "StorageCell" + dim, "scaleZ", 0 ).set( targetZ );
		save();
	}

	/**
	 * lazy loading, can load any id, even ones that don't exist anymore.
	 * 
	 * @param storageID
	 * @return
	 */
	public GridStorage getGridStorage(long storageID)
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

	public void destroyGridStorage(long id)
	{
		this.getCategory( "gridstorage" ).remove( "" + id );
	}

	@Override
	public void save()
	{
		// populate new data
		for (GridStorageSearch gs : loadedStorage.keySet())
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

	private long nextGridStorage()
	{
		long r = lastGridStorage++;
		get( "Counters", "lastGridStorage", lastGridStorage ).set( Long.toString( lastGridStorage ) );
		return r;
	}

	private long nextPlayer()
	{
		long r = lastPlayer++;
		get( "Counters", "lastPlayer", lastPlayer ).set( lastPlayer );
		return r;
	}

	public int getNextOrderedValue(String name)
	{
		Property p = this.get( "orderedValues", name, 0 );
		int myValue = p.getInt();
		p.set( myValue + 1 );
		return myValue;
	}

	public int getPlayerID(GameProfile profile)
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
			getUUIDMap().put( prop.getInt(), profile.getId() ); // add to reverse map
			save();
			return prop.getInt();
		}
	}

	public HashMap<Integer, UUID> getUUIDMap()
	{
		if ( idToUUID == null )
		{
			idToUUID = new HashMap<Integer, UUID>();

			ConfigCategory playerList = this.getCategory( "players" );

			for (Entry<String, Property> b : playerList.getValues().entrySet())
				idToUUID.put( b.getValue().getInt(), UUID.fromString( b.getKey() ) );
		}

		return idToUUID;
	}

	public EntityPlayer getPlayerFromID(int playerID)
	{
		UUID id = getUUIDMap().get( playerID );

		if ( id != null )
		{
			for (EntityPlayer player : CommonHelper.proxy.getPlayers())
			{
				if ( player.getUniqueID().equals( id ) )
					return player;
			}
		}

		return null;
	}
}
