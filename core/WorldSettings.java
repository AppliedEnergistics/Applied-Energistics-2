package appeng.core;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import appeng.api.util.WorldCoord;
import appeng.me.GridStorage;
import appeng.me.GridStorageSearch;
import appeng.services.CompassService;

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
		(new Thread( compass, "AE Compass Service" )).start();

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

	public void sendToPlayer(EntityPlayer player)
	{

	}

	public void init()
	{
		save();
	}

	public void shutdown()
	{
		save();
		compass.kill();
		instance = null;
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

	public String getUsername(int id)
	{
		ConfigCategory playerList = this.getCategory( "players" );
		for (Entry<String, Property> fish : playerList.entrySet())
		{
			if ( fish.getValue().isIntValue() && fish.getValue().getInt() == id )
				return fish.getKey();
		}
		return null;
	}

	public int getPlayerID(String username)
	{
		ConfigCategory playerList = this.getCategory( "players" );
		if ( playerList == null || username == null || username.length() == 0 )
			return -1;

		Property prop = playerList.get( username );
		if ( prop != null && prop.isIntValue() )
			return prop.getInt();
		else
		{
			playerList.put( username, prop = new Property( username, "" + nextPlayer(), Property.Type.INTEGER ) );
			save();
			return prop.getInt();
		}
	}

}
