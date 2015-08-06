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

package appeng.services;


import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import appeng.api.AEApi;
import appeng.api.util.DimensionalCoord;
import appeng.services.compass.CompassReader;
import appeng.services.compass.ICompassCallback;


public final class CompassService
{
	private static final int CHUNK_SIZE = 16;

	private final Map<World, CompassReader> worldSet = new HashMap<World, CompassReader>( 10 );
	private final ExecutorService executor;

	/**
	 * AE2 Folder for each world
	 */
	private final File worldCompassFolder;

	private int jobSize;

	public CompassService( @Nonnull final File worldCompassFolder, @Nonnull final ThreadFactory factory )
	{
		Preconditions.checkNotNull( worldCompassFolder );

		this.worldCompassFolder = worldCompassFolder;
		this.executor = Executors.newSingleThreadExecutor( factory );
		this.jobSize = 0;
	}

	public Future<?> getCompassDirection( DimensionalCoord coord, int maxRange, ICompassCallback cc )
	{
		this.jobSize++;
		return this.executor.submit( new CMDirectionRequest( coord, maxRange, cc ) );
	}

	public int jobSize()
	{
		return this.jobSize;
	}

	public void cleanUp()
	{
		for( CompassReader cr : this.worldSet.values() )
		{
			cr.close();
		}
	}

	public void updateArea( World w, int chunkX, int chunkZ )
	{
		int x = chunkX << 4;
		int z = chunkZ << 4;

		this.updateArea( w, x, CHUNK_SIZE, z );
		this.updateArea( w, x, CHUNK_SIZE + 32, z );
		this.updateArea( w, x, CHUNK_SIZE + 64, z );
		this.updateArea( w, x, CHUNK_SIZE + 96, z );

		this.updateArea( w, x, CHUNK_SIZE + 128, z );
		this.updateArea( w, x, CHUNK_SIZE + 160, z );
		this.updateArea( w, x, CHUNK_SIZE + 192, z );
		this.updateArea( w, x, CHUNK_SIZE + 224, z );
	}

	public Future<?> updateArea( World w, int x, int y, int z )
	{
		this.jobSize++;

		int cx = x >> 4;
		int cdy = y >> 5;
		int cz = z >> 4;

		int low_y = cdy << 5;
		int hi_y = low_y + 32;

		// lower level...
		Chunk c = w.getChunkFromChunkCoords( cx, cz );

		for( Block skyStoneBlock : AEApi.instance().definitions().blocks().skyStone().maybeBlock().asSet() )
		{
			for( int i = 0; i < CHUNK_SIZE; i++ )
			{
				for( int j = 0; j < CHUNK_SIZE; j++ )
				{
					for( int k = low_y; k < hi_y; k++ )
					{
						Block blk = c.getBlock( i, k, j );
						if( blk == skyStoneBlock )
						{
							return this.executor.submit( new CMUpdatePost( w, cx, cz, cdy, true ) );
						}
					}
				}
			}
		}

		return this.executor.submit( new CMUpdatePost( w, cx, cz, cdy, false ) );
	}

	private CompassReader getReader( World w )
	{
		CompassReader cr = this.worldSet.get( w );

		if( cr == null )
		{
			cr = new CompassReader( w.provider.getDimensionId(), this.worldCompassFolder );
			this.worldSet.put( w, cr );
		}

		return cr;
	}

	private int dist( int ax, int az, int bx, int bz )
	{
		int up = ( bz - az ) * CHUNK_SIZE;
		int side = ( bx - ax ) * CHUNK_SIZE;

		return up * up + side * side;
	}

	private double rad( int ax, int az, int bx, int bz )
	{
		int up = bz - az;
		int side = bx - ax;

		return Math.atan2( -up, side ) - Math.PI / 2.0;
	}

	public void kill()
	{
		this.executor.shutdown();

		try
		{
			this.executor.awaitTermination( 6, TimeUnit.MINUTES );
			this.jobSize = 0;

			for( CompassReader cr : this.worldSet.values() )
			{
				cr.close();
			}

			this.worldSet.clear();
		}
		catch( InterruptedException e )
		{
			// wrap this up..
		}
	}

	private class CMUpdatePost implements Runnable
	{

		public final World world;

		public final int chunkX;
		public final int chunkZ;
		public final int doubleChunkY; // 32 blocks instead of 16.
		public final boolean value;

		public CMUpdatePost( World w, int cx, int cz, int dcy, boolean val )
		{
			this.world = w;
			this.chunkX = cx;
			this.doubleChunkY = dcy;
			this.chunkZ = cz;
			this.value = val;
		}

		@Override
		public void run()
		{
			CompassService.this.jobSize--;

			CompassReader cr = CompassService.this.getReader( this.world );
			cr.setHasBeacon( this.chunkX, this.chunkZ, this.doubleChunkY, this.value );

			if( CompassService.this.jobSize() < 2 )
			{
				CompassService.this.cleanUp();
			}
		}
	}


	private class CMDirectionRequest implements Runnable
	{

		public final int maxRange;
		public final DimensionalCoord coord;
		public final ICompassCallback callback;

		public CMDirectionRequest( DimensionalCoord coord, int getMaxRange, ICompassCallback cc )
		{
			this.coord = coord;
			this.maxRange = getMaxRange;
			this.callback = cc;
		}

		@Override
		public void run()
		{
			CompassService.this.jobSize--;

			int cx = this.coord.x >> 4;
			int cz = this.coord.z >> 4;

			CompassReader cr = CompassService.this.getReader( this.coord.getWorld() );

			// Am I standing on it?
			if( cr.hasBeacon( cx, cz ) )
			{
				this.callback.calculatedDirection( true, true, -999, 0 );

				if( CompassService.this.jobSize() < 2 )
				{
					CompassService.this.cleanUp();
				}

				return;
			}

			// spiral outward...
			for( int offset = 1; offset < this.maxRange; offset++ )
			{
				int minX = cx - offset;
				int minZ = cz - offset;
				int maxX = cx + offset;
				int maxZ = cz + offset;

				int closest = Integer.MAX_VALUE;
				int chosen_x = cx;
				int chosen_z = cz;

				for( int z = minZ; z <= maxZ; z++ )
				{
					if( cr.hasBeacon( minX, z ) )
					{
						int closeness = CompassService.this.dist( cx, cz, minX, z );
						if( closeness < closest )
						{
							closest = closeness;
							chosen_x = minX;
							chosen_z = z;
						}
					}

					if( cr.hasBeacon( maxX, z ) )
					{
						int closeness = CompassService.this.dist( cx, cz, maxX, z );
						if( closeness < closest )
						{
							closest = closeness;
							chosen_x = maxX;
							chosen_z = z;
						}
					}
				}

				for( int x = minX + 1; x < maxX; x++ )
				{
					if( cr.hasBeacon( x, minZ ) )
					{
						int closeness = CompassService.this.dist( cx, cz, x, minZ );
						if( closeness < closest )
						{
							closest = closeness;
							chosen_x = x;
							chosen_z = minZ;
						}
					}

					if( cr.hasBeacon( x, maxZ ) )
					{
						int closeness = CompassService.this.dist( cx, cz, x, maxZ );
						if( closeness < closest )
						{
							closest = closeness;
							chosen_x = x;
							chosen_z = maxZ;
						}
					}
				}

				if( closest < Integer.MAX_VALUE )
				{
					this.callback.calculatedDirection( true, false, CompassService.this.rad( cx, cz, chosen_x, chosen_z ), CompassService.this.dist( cx, cz, chosen_x, chosen_z ) );

					if( CompassService.this.jobSize() < 2 )
					{
						CompassService.this.cleanUp();
					}

					return;
				}
			}

			// didn't find shit...
			this.callback.calculatedDirection( false, true, -999, 999 );

			if( CompassService.this.jobSize() < 2 )
			{
				CompassService.this.cleanUp();
			}
		}
	}
}
