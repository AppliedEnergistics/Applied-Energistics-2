package appeng.hooks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketCompassRequest;

public class CompassManager
{

	public static CompassManager instance = new CompassManager();

	class CompassReq
	{

		final int hash;

		final long attunement;
		final int cx, cdy, cz;

		public CompassReq(long attunement, int x, int y, int z) {
			this.attunement = attunement;
			cx = x >> 4;
			cdy = y >> 5;
			cz = z >> 4;
			hash = ((Integer) cx).hashCode() ^ ((Integer) cdy).hashCode() ^ ((Integer) cz).hashCode() ^ ((Long) attunement).hashCode();
		}

		@Override
		public int hashCode()
		{
			return hash;
		}

		@Override
		public boolean equals(Object obj)
		{
			CompassReq b = (CompassReq) obj;
			return attunement == b.attunement && cx == b.cx && cdy == b.cdy && cz == b.cz;
		}

	}

	HashMap<CompassReq, CompassResult> reqs = new HashMap();

	public void postResult(long attunement, int x, int y, int z, CompassResult res)
	{
		AELog.info( "CompassManager.postResult" );
		CompassReq r = new CompassReq( attunement, x, y, z );
		reqs.put( r, res );
	}

	public CompassResult getCompassDirection(long attunement, int x, int y, int z)
	{
		long now = System.currentTimeMillis();

		Iterator<CompassResult> i = reqs.values().iterator();
		while (i.hasNext())
		{
			CompassResult res = i.next();
			long diff = now - res.time;
			if ( diff > 20000 )
				i.remove();
		}

		CompassReq r = new CompassReq( attunement, x, y, z );
		CompassResult res = reqs.get( r );

		if ( res == null )
		{
			res = new CompassResult( false, true, 0 );
			reqs.put( r, res );
			requestUpdate( r );
		}
		else if ( now - res.time > 1000 * 3 )
		{
			if ( !res.requested )
			{
				res.requested = true;
				requestUpdate( r );
			}
		}

		return res;
	}

	private void requestUpdate(CompassReq r)
	{
		AELog.info( "CompassManager.requestUpdate" );

		try
		{
			NetworkHandler.instance.sendToServer( new PacketCompassRequest( r.attunement, r.cx, r.cz, r.cdy ) );
		}
		catch (IOException e)
		{
			AELog.error( e );
		}
	}

}
