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

package appeng.services;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import appeng.api.AEApi;
import appeng.api.util.DimensionalCoord;
import appeng.services.helpers.CompassReader;
import appeng.services.helpers.ICompassCallback;

public class CompassService implements ThreadFactory
{

	int jobSize = 0;

	private class CMUpdatePost implements Runnable
	{

		public final World world;

		public final int chunkX, chunkZ;
		public final int doubleChunkY; // 32 blocks instead of 16.
		public final boolean value;

		public CMUpdatePost(World w, int cx, int cz, int dcy, boolean val) {
			world = w;
			chunkX = cx;
			doubleChunkY = dcy;
			chunkZ = cz;
			value = val;
		}

		@Override
		public void run()
		{
			jobSize--;

			CompassReader cr = getReader( world );
			cr.setHasBeacon( chunkX, chunkZ, doubleChunkY, value );

			if ( jobSize() < 2 )
				cleanUp();
		}

	}

	private class CMDirectionRequest implements Runnable
	{

		public final int maxRange;
		public final DimensionalCoord coord;
		public final ICompassCallback callback;

		public CMDirectionRequest(DimensionalCoord coord, int getMaxRange, ICompassCallback cc) {
			this.coord = coord;
			this.maxRange = getMaxRange;
			callback = cc;
		}

		@Override
		public void run()
		{
			jobSize--;

			int cx = coord.x >> 4;
			int cz = coord.z >> 4;

			CompassReader cr = getReader( coord.getWorld() );

			// Am I standing on it?
			if ( cr.hasBeacon( cx, cz ) )
			{
				callback.calculatedDirection( true, true, -999, 0 );

				if ( jobSize() < 2 )
					cleanUp();

				return;
			}

			// spiral outward...
			for (int offset = 1; offset < maxRange; offset++)
			{
				int minX = cx - offset;
				int minZ = cz - offset;
				int maxX = cx + offset;
				int maxZ = cz + offset;

				int closest = Integer.MAX_VALUE;
				int chosen_x = cx;
				int chosen_z = cz;

				for (int z = minZ; z <= maxZ; z++)
				{
					if ( cr.hasBeacon( minX, z ) )
					{
						int closeness = dist( cx, cz, minX, z );
						if ( closeness < closest )
						{
							closest = closeness;
							chosen_x = minX;
							chosen_z = z;
						}
					}

					if ( cr.hasBeacon( maxX, z ) )
					{
						int closeness = dist( cx, cz, maxX, z );
						if ( closeness < closest )
						{
							closest = closeness;
							chosen_x = maxX;
							chosen_z = z;
						}
					}
				}

				for (int x = minX + 1; x < maxX; x++)
				{
					if ( cr.hasBeacon( x, minZ ) )
					{
						int closeness = dist( cx, cz, x, minZ );
						if ( closeness < closest )
						{
							closest = closeness;
							chosen_x = x;
							chosen_z = minZ;
						}
					}

					if ( cr.hasBeacon( x, maxZ ) )
					{
						int closeness = dist( cx, cz, x, maxZ );
						if ( closeness < closest )
						{
							closest = closeness;
							chosen_x = x;
							chosen_z = maxZ;
						}
					}
				}

				if ( closest < Integer.MAX_VALUE )
				{
					callback.calculatedDirection( true, false, rad( cx, cz, chosen_x, chosen_z ), dist( cx, cz, chosen_x, chosen_z ) );

					if ( jobSize() < 2 )
						cleanUp();

					return;
				}
			}

			// didn't find shit...
			callback.calculatedDirection( false, true, -999, 999 );

			if ( jobSize() < 2 )
				cleanUp();
		}
	}

	public Future<?> getCompassDirection(DimensionalCoord coord, int maxRange, ICompassCallback cc)
	{
		jobSize++;
		return executor.submit( new CMDirectionRequest( coord, maxRange, cc ) );
	}

	public int jobSize()
	{
		return jobSize;
	}

	public void cleanUp()
	{
		for (CompassReader cr : worldSet.values())
			cr.close();
	}

	public void updateArea(World w, int chunkX, int chunkZ)
	{
		int x = chunkX << 4;
		int z = chunkZ << 4;

		updateArea( w, x, 16, z );
		updateArea( w, x, 16 + 32, z );
		updateArea( w, x, 16 + 64, z );
		updateArea( w, x, 16 + 96, z );

		updateArea( w, x, 16 + 128, z );
		updateArea( w, x, 16 + 160, z );
		updateArea( w, x, 16 + 192, z );
		updateArea( w, x, 16 + 224, z );
	}

	public Future<?> updateArea(World w, int x, int y, int z)
	{
		jobSize++;

		int cx = x >> 4;
		int cdy = y >> 5;
		int cz = z >> 4;

		int low_y = cdy << 5;
		int hi_y = low_y + 32;

		Block skystone = AEApi.instance().blocks().blockSkyStone.block();

		// lower level...
		Chunk c = w.getChunkFromBlockCoords( x, z );

		for (int i = 0; i < 16; i++)
		{
			for (int j = 0; j < 16; j++)
			{
				for (int k = low_y; k < hi_y; k++)
				{
					Block blk = c.getBlock( i, k, j );
					if ( blk == skystone && c.getBlockMetadata( i, k, j ) == 0 )
					{
						return executor.submit( new CMUpdatePost( w, cx, cz, cdy, true ) );
					}
				}
			}
		}

		return executor.submit( new CMUpdatePost( w, cx, cz, cdy, false ) );
	}

	final HashMap<World, CompassReader> worldSet = new HashMap<World, CompassReader>();
	final ExecutorService executor;

	final File rootFolder;

	public CompassService(File aEFolder) {
		rootFolder = aEFolder;
		executor = Executors.newSingleThreadExecutor( this );
		jobSize = 0;
	}

	private CompassReader getReader(World w)
	{
		CompassReader cr = worldSet.get( w );

		if ( cr == null )
		{
			cr = new CompassReader( w.provider.dimensionId, rootFolder );
			worldSet.put( w, cr );
		}

		return cr;
	}

	private int dist(int ax, int az, int bx, int bz)
	{
		int up = (bz - az) * 16;
		int side = (bx - ax) * 16;

		return up * up + side * side;
	}

	private double rad(int ax, int az, int bx, int bz)
	{
		int up = bz - az;
		int side = bx - ax;

		return Math.atan2( -up, side ) - Math.PI / 2.0;
	}

	public void kill()
	{
		executor.shutdown();

		try
		{
			executor.awaitTermination( 6, TimeUnit.MINUTES );
			jobSize = 0;

			for (CompassReader cr : worldSet.values())
			{
				cr.close();
			}

			worldSet.clear();
		}
		catch (InterruptedException e)
		{
			// wrap this up..
		}
	}

	@Override
	public Thread newThread(Runnable job)
	{
		return new Thread( job, "AE Compass Service" );
	}
}
