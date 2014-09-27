package appeng.me;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridStorage;
import appeng.core.AELog;
import appeng.core.WorldSettings;

public class GridStorage implements IGridStorage
{

	private WeakReference<IGrid> internalGrid = null;

	final long myID;
	final NBTTagCompound data;

	public boolean isDirty = false;
	private WeakHashMap<GridStorage,Boolean> divlist = new WeakHashMap();
	final GridStorageSearch mySearchEntry; // keep myself in the list until I'm
											// lost...

	/**
	 * for use with world settings
	 * 
	 * @param id
	 * @param gss
	 */
	public GridStorage(long id, GridStorageSearch gss) {
		myID = id;
		mySearchEntry = gss;
		data = new NBTTagCompound();
	}

	/**
	 * for use with world settings
	 * 
	 * @param input
	 * @param id
	 * @param gss
	 */
	public GridStorage(String input, long id, GridStorageSearch gss) {
		myID = id;
		mySearchEntry = gss;
		NBTTagCompound myTag = null;

		try
		{
			byte[] dbata = javax.xml.bind.DatatypeConverter.parseBase64Binary( input );
			myTag = CompressedStreamTools.readCompressed( new ByteArrayInputStream( dbata ) );
		}
		catch (Throwable t)
		{
			myTag = new NBTTagCompound();
		}

		data = myTag;
	}

	/**
	 * fake storage.
	 */
	public GridStorage() {
		myID = 0;
		mySearchEntry = null;
		data = new NBTTagCompound();
	}

	public String getValue()
	{
		isDirty = false;

		Grid currentGrid = (Grid) getGrid();
		if ( currentGrid != null )
		{
			currentGrid.saveState();
		}

		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			CompressedStreamTools.writeCompressed( data, out );
			return javax.xml.bind.DatatypeConverter.printBase64Binary( out.toByteArray() );
		}
		catch (IOException e)
		{
			AELog.error( e );
		}

		return "";
	}

	@Override
	public NBTTagCompound dataObject()
	{
		return data;
	}

	@Override
	public long getID()
	{
		return myID;
	}

	public void markDirty()
	{
		isDirty = true;
	}

	public IGrid getGrid()
	{
		return internalGrid == null ? null : internalGrid.get();
	}

	public void setGrid(Grid grid)
	{
		internalGrid = new WeakReference<IGrid>( grid );
	}

	public void addDivided(GridStorage gs)
	{
		divlist.put( gs, true );
	}

	public boolean hasDivided(GridStorage myStorage)
	{
		return divlist.containsKey( myStorage );
	}

	public void remove()
	{
		WorldSettings.getInstance().destroyGridStorage( getID() );
	}

}
