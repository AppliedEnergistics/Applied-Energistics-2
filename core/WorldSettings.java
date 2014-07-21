package appeng.core;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayerMP;
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

	public WorldSettings(File aeFolder) {
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

			File f = new File( world.getPath() + File.separatorChar + "AE2" );

			if ( !f.exists() || !f.isDirectory() )
				f.mkdir();

			instance = new WorldSettings( f );
		}

		return instance;
	}

	public void sendToPlayer(EntityPlayerMP player)
	{
		for (int newDim : get( "DimensionManager", "StorageCells", new int[0] ).getIntList())
		{
			try
			{
				NetworkHandler.instance.sendTo( new PacketNewStorageDimension( newDim ), player );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}
		}

		for (PlayerColor pc : TickHandler.instance.getPlayerColors().values())
			NetworkHandler.instance.sendToAll( pc.getPacket() );
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
			save();
			return prop.getInt();
		}
	}

}
