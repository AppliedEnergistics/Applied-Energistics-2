package appeng.services;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.util.DimensionalCoord;
import appeng.services.helpers.CompassException;
import appeng.services.helpers.CompassReader;
import appeng.services.helpers.ICompassCallback;

public class CompassService implements Runnable
{

	private class CompassMessage
	{

		public boolean isUpdate()
		{
			return false;
		}

		public boolean isRequest()
		{
			return false;
		}

	};

	private class CMUpdatePost extends CompassMessage
	{

		public final World world;

		public final int chunkX, chunkZ;
		public final int doubleChunkY; // 32 blocks instead of 16.
		public final boolean value;

		@Override
		public boolean isUpdate()
		{
			return true;
		}

		public CMUpdatePost(World w, int cx, int cz, int dcy, boolean val) {
			world = w;
			chunkX = cx;
			doubleChunkY = dcy;
			chunkZ = cz;
			value = val;
		}
	};

	private class CMDirectionRequest extends CompassMessage
	{

		public final DimensionalCoord coord;
		public final int maxRange;
		public final ICompassCallback callback;

		@Override
		public boolean isRequest()
		{
			return true;
		}

		public CMDirectionRequest(DimensionalCoord coord, int getMaxRange, ICompassCallback cc) {
			this.coord = coord;
			this.maxRange = getMaxRange;
			callback = cc;
		}

	};

	private LinkedList<CompassMessage> jobList = new LinkedList();

	public void updateArea(World w, int x, int y, int z)
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
						postJob( new CMUpdatePost( w, cx, cz, cdy, true ) );
						return;
					}
				}
			}
		}

		postJob( new CMUpdatePost( w, cx, cz, cdy, false ) );
	}

	public void getCompassDirection(DimensionalCoord coord, int maxRange, ICompassCallback cc)
	{
		postJob( new CMDirectionRequest( coord, maxRange, cc ) );
	}

	private void postJob(CompassMessage msg)
	{
		synchronized (jobList)
		{
			if ( msg != null )
				jobList.offer( msg );
			jobList.notify();
		}
	}

	private CompassMessage getNextMessage()
	{
		CompassMessage myMsg = null;

		while (myMsg == null && run)
		{
			synchronized (jobList)
			{
				try
				{
					myMsg = jobList.poll();
					overOberdened = jobList.isEmpty();

					if ( myMsg == null )
						jobList.wait();
				}
				catch (InterruptedException e)
				{
					// :P
				}
			}
		}

		return myMsg;
	}

	boolean overOberdened = false;
	HashMap<World, CompassReader> worldSet = new HashMap();

	final File rootFolder;

	public CompassService(File aEFolder) {
		rootFolder = aEFolder;
	}

	public CompassReader getReader(World w)
	{
		CompassReader cr = worldSet.get( w );

		if ( cr == null )
		{
			cr = new CompassReader( w, rootFolder );
			worldSet.put( w, cr );
		}

		return cr;
	}

	private void processRequest(CMDirectionRequest req)
	{
		int cx = req.coord.x >> 4;
		int cz = req.coord.z >> 4;

		CompassReader cr = getReader( req.coord.getWorld() );

		// Am I standing on it?
		if ( cr.hasBeacon( cx, cz ) )
		{
			req.callback.calculatedDirection( true, true, -999, 0 );
			return;
		}

		// spiral outward...
		for (int offset = 1; offset < 174; offset++)
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
				req.callback.calculatedDirection( true, false, rad( cx, cz, chosen_x, chosen_z ), dist( cx, cz, chosen_x, chosen_z ) );
				if ( !overOberdened )
					cr.close();
				return;
			}
		}

		// didn't find shit...
		req.callback.calculatedDirection( false, true, -999, 999 );
		if ( !overOberdened )
			cr.close();
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

	private void processUpdate(CMUpdatePost req)
	{
		CompassReader cr = getReader( req.world );
		cr.setHasBeacon( req.chunkX, req.chunkZ, req.doubleChunkY, req.value );
		cr.close();
	}

	boolean run = true;
	boolean stopped = false;

	@Override
	public void run()
	{

		while (run)
		{
			CompassMessage myMsg = getNextMessage();

			try
			{
				if ( myMsg != null )
				{
					if ( myMsg.isRequest() )
						processRequest( (CMDirectionRequest) myMsg );
					else if ( myMsg.isUpdate() )
						processUpdate( (CMUpdatePost) myMsg );
				}
			}
			catch (CompassException ce)
			{
				ce.inner.printStackTrace();
			}
		}

		stopped = true;
	}

	public void kill()
	{
		run = false;
		postJob( null );

		while (!stopped)
		{
			try
			{
				Thread.sleep( 100 );
			}
			catch (InterruptedException e)
			{
				// :P
			}
		}
	}
}
