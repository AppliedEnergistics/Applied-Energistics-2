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
import appeng.api.AEApi;
import appeng.api.util.DimensionalCoord;
import appeng.services.helpers.CompassReader;
import appeng.services.helpers.ICompassCallback;

public class CompassService implements ThreadFactory
{

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
			CompassReader cr = getReader( world );
			cr.setHasBeacon( chunkX, chunkZ, doubleChunkY, value );
			cr.close();
		}

	};

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
			int cx = coord.x >> 4;
			int cz = coord.z >> 4;

			CompassReader cr = getReader( coord.getWorld() );

			// Am I standing on it?
			if ( cr.hasBeacon( cx, cz ) )
			{
				callback.calculatedDirection( true, true, -999, 0 );
				return;
			}

			// spiral outward...
			for (int offset = 1; offset < maxRange; offset++)
			{
				int minx = cx - offset;
				int minz = cz - offset;
				int maxx = cx + offset;
				int maxz = cz + offset;

				int closest = Integer.MAX_VALUE;
				int chosen_x = cx;
				int chosen_z = cz;

				for (int z = minz; z <= maxz; z++)
				{
					if ( cr.hasBeacon( minx, z ) )
					{
						int closness = dist( cx, cz, minx, z );
						if ( closness < closest )
						{
							closest = closness;
							chosen_x = minx;
							chosen_z = z;
						}
					}

					if ( cr.hasBeacon( maxx, z ) )
					{
						int closness = dist( cx, cz, maxx, z );
						if ( closness < closest )
						{
							closest = closness;
							chosen_x = maxx;
							chosen_z = z;
						}
					}
				}

				for (int x = minx + 1; x < maxx; x++)
				{
					if ( cr.hasBeacon( x, minz ) )
					{
						int closness = dist( cx, cz, x, minz );
						if ( closness < closest )
						{
							closest = closness;
							chosen_x = x;
							chosen_z = minz;
						}
					}

					if ( cr.hasBeacon( x, maxz ) )
					{
						int closness = dist( cx, cz, x, maxz );
						if ( closness < closest )
						{
							closest = closness;
							chosen_x = x;
							chosen_z = maxz;
						}
					}
				}

				if ( closest < Integer.MAX_VALUE )
				{
					callback.calculatedDirection( true, false, rad( cx, cz, chosen_x, chosen_z ), dist( cx, cz, chosen_x, chosen_z ) );
					return;
				}
			}

			// didn't find shit...
			callback.calculatedDirection( false, true, -999, 999 );
		}
	};

	public Future<?> getCompassDirection(DimensionalCoord coord, int maxRange, ICompassCallback cc)
	{
		return executor.submit( new CMDirectionRequest( coord, maxRange, cc ) );
	}

	public Future<?> updateArea(World w, int x, int y, int z)
	{
		int cx = x >> 4;
		int cdy = y >> 5;
		int cz = z >> 4;

		int low_x = cx << 4;
		int low_z = cz << 4;
		int low_y = cdy << 5;

		int hi_x = low_x + 16;
		int hi_z = low_z + 16;
		int hi_y = low_y + 32;

		Block skystone = AEApi.instance().blocks().blockSkyStone.block();

		for (int i = low_x; i < hi_x; i++)
		{
			for (int j = low_z; j < hi_z; j++)
			{
				for (int k = low_y; k < hi_y; k++)
				{
					Block blk = w.getBlock( i, k, j );
					if ( blk == skystone && w.getBlockMetadata( i, k, j ) == 0 )
					{
						return executor.submit( new CMUpdatePost( w, cx, cz, cdy, true ) );
					}
				}
			}
		}

		return executor.submit( new CMUpdatePost( w, cx, cz, cdy, false ) );
	}

	HashMap<World, CompassReader> worldSet = new HashMap();
	ExecutorService executor;

	final File rootFolder;

	public CompassService(File aEFolder) {
		rootFolder = aEFolder;
		executor = Executors.newSingleThreadExecutor( this );
	}

	private CompassReader getReader(World w)
	{
		CompassReader cr = worldSet.get( w );

		if ( cr == null )
		{
			cr = new CompassReader( w, rootFolder );
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
