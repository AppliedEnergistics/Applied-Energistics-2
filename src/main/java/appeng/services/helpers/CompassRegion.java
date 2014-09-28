package appeng.services.helpers;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import appeng.core.AELog;

public class CompassRegion
{

	final int low_x;
	final int low_z;

	final int hi_x;
	final int hi_z;

	final int world;

	boolean hasFile = false;
	final File rootFolder;
	RandomAccessFile raf = null;
	ByteBuffer buffer;

	public void close()
	{
		try
		{
			if ( hasFile )
			{
				buffer = null;
				raf.close();
				raf = null;
				hasFile = false;
			}
		}
		catch (Throwable t)
		{
			throw new CompassException( t );
		}
	}

	public CompassRegion(int cx, int cz, int worldID, File rootFolder) {

		world = worldID;
		this.rootFolder = rootFolder;

		int region_x = cx >> 10;
		int region_z = cz >> 10;

		low_x = region_x << 10;
		low_z = region_z << 10;

		hi_x = low_x + 1024;
		hi_z = low_z + 1024;

		openFile( false );
	}

	public boolean hasBeacon(int cx, int cz)
	{
		if ( hasFile )
		{
			cx = cx & 0x3FF;
			cz = cz & 0x3FF;

			int val = read( cx, cz );
			if ( val != 0 )
				return true;
		}

		return false;
	}

	public void setHasBeacon(int cx, int cz, int cdy, boolean hasBeacon)
	{
		cx &= 0x3FF;
		cz &= 0x3FF;

		openFile( hasBeacon );

		if ( hasFile )
		{
			int val = read( cx, cz );
			int originalVal = val;

			if ( hasBeacon )
				val |= 1 << cdy;
			else
				val &= ~(1 << cdy);

			if ( originalVal != val )
				write( cx, cz, val );
		}
	}

	private void write(int cx, int cz, int val)
	{
		try
		{
			buffer.put( cx + cz * 0x400, (byte) val );
			// raf.seek( cx + cz * 0x400 );
			// raf.writeByte( val );
		}
		catch (Throwable t)
		{
			throw new CompassException( t );
		}
	}

	private int read(int cx, int cz)
	{
		try
		{
			return buffer.get( cx + cz * 0x400 );
			// raf.seek( cx + cz * 0x400 );
			// return raf.readByte();
		}
		catch (IndexOutOfBoundsException outOfBounds)
		{
			return 0;
		}
		catch (Throwable t)
		{
			throw new CompassException( t );
		}
	}

	private void openFile(boolean create)
	{
		File fName = getFileName();
		if ( hasFile )
			return;

		if ( create || fileExists( fName ) )
		{
			try
			{
				raf = new RandomAccessFile( fName, "rw" );
				FileChannel fc = raf.getChannel();
				buffer = fc.map( FileChannel.MapMode.READ_WRITE, 0, 0x400 * 0x400 );// fc.size() );
				hasFile = true;
			}
			catch (Throwable t)
			{
				throw new CompassException( t );
			}
		}

	}

	private boolean fileExists(File name)
	{
		return name.exists() && name.isFile();
	}

	private File getFileName()
	{
		String folder = rootFolder.getPath() + File.separatorChar + "compass";
		File folderFile = new File( folder );

		if ( !folderFile.exists() || !folderFile.isDirectory() )
		{
			if ( !folderFile.mkdir() )
				AELog.info( "Failed to created AE2/compass/" );
		}

		return new File( folder + File.separatorChar + world + "_" + low_x + "_" + low_z + ".dat" );
	}

}
