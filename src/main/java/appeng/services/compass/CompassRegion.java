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

package appeng.services.compass;


import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import appeng.core.AELog;


public final class CompassRegion
{
	final int low_x;
	final int low_z;

	final int hi_x;
	final int hi_z;

	final int world;
	final File rootFolder;
	boolean hasFile = false;
	RandomAccessFile raf = null;
	ByteBuffer buffer;

	public CompassRegion( int cx, int cz, int worldID, File rootFolder )
	{

		this.world = worldID;
		this.rootFolder = rootFolder;

		int region_x = cx >> 10;
		int region_z = cz >> 10;

		this.low_x = region_x << 10;
		this.low_z = region_z << 10;

		this.hi_x = this.low_x + 1024;
		this.hi_z = this.low_z + 1024;

		this.openFile( false );
	}

	private void openFile( boolean create )
	{
		File fName = this.getFileName();
		if( this.hasFile )
		{
			return;
		}

		if( create || this.fileExists( fName ) )
		{
			try
			{
				this.raf = new RandomAccessFile( fName, "rw" );
				FileChannel fc = this.raf.getChannel();
				this.buffer = fc.map( FileChannel.MapMode.READ_WRITE, 0, 0x400 * 0x400 );// fc.size() );
				this.hasFile = true;
			}
			catch( Throwable t )
			{
				throw new CompassException( t );
			}
		}
	}

	private File getFileName()
	{
		String folder = this.rootFolder.getPath() + File.separatorChar + "compass";
		File folderFile = new File( folder );

		if( !folderFile.exists() || !folderFile.isDirectory() )
		{
			if( !folderFile.mkdir() )
			{
				AELog.info( "Failed to create AE2/compass/" );
			}
		}

		return new File( folder, this.world + '_' + this.low_x + '_' + this.low_z + ".dat" );
	}

	private boolean fileExists( File name )
	{
		return name.exists() && name.isFile();
	}

	public final void close()
	{
		try
		{
			if( this.hasFile )
			{
				this.buffer = null;
				this.raf.close();
				this.raf = null;
				this.hasFile = false;
			}
		}
		catch( Throwable t )
		{
			throw new CompassException( t );
		}
	}

	public final boolean hasBeacon( int cx, int cz )
	{
		if( this.hasFile )
		{
			cx &= 0x3FF;
			cz &= 0x3FF;

			int val = this.read( cx, cz );
			if( val != 0 )
			{
				return true;
			}
		}

		return false;
	}

	private int read( int cx, int cz )
	{
		try
		{
			return this.buffer.get( cx + cz * 0x400 );
			// raf.seek( cx + cz * 0x400 );
			// return raf.readByte();
		}
		catch( IndexOutOfBoundsException outOfBounds )
		{
			return 0;
		}
		catch( Throwable t )
		{
			throw new CompassException( t );
		}
	}

	public final void setHasBeacon( int cx, int cz, int cdy, boolean hasBeacon )
	{
		cx &= 0x3FF;
		cz &= 0x3FF;

		this.openFile( hasBeacon );

		if( this.hasFile )
		{
			int val = this.read( cx, cz );
			int originalVal = val;

			if( hasBeacon )
			{
				val |= 1 << cdy;
			}
			else
			{
				val &= ~( 1 << cdy );
			}

			if( originalVal != val )
			{
				this.write( cx, cz, val );
			}
		}
	}

	private void write( int cx, int cz, int val )
	{
		try
		{
			this.buffer.put( cx + cz * 0x400, (byte) val );
			// raf.seek( cx + cz * 0x400 );
			// raf.writeByte( val );
		}
		catch( Throwable t )
		{
			throw new CompassException( t );
		}
	}
}
