package appeng.hooks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketCompassRequest;

public class CompassManager
{

	public static final CompassManager instance = new CompassManager();

	class CompassRequest
	{

		final int hash;

		final long attunement;
		final int cx, cdy, cz;

		public CompassRequest(long attunement, int x, int y, int z) {
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
			if ( obj == null )
				return false;
			if ( getClass() != obj.getClass() )
				return false;
			CompassRequest other = (CompassRequest) obj;
			return attunement == other.attunement && cx == other.cx && cdy == other.cdy && cz == other.cz;
		}

	}

	final HashMap<CompassRequest, CompassResult> requests = new HashMap<CompassRequest, CompassResult>();

	public void postResult(long attunement, int x, int y, int z, CompassResult result)
	{
		CompassRequest r = new CompassRequest( attunement, x, y, z );
		requests.put( r, result );
	}

	public CompassResult getCompassDirection(long attunement, int x, int y, int z)
	{
		long now = System.currentTimeMillis();

		Iterator<CompassResult> i = requests.values().iterator();
		while (i.hasNext())
		{
			CompassResult res = i.next();
			long diff = now - res.time;
			if ( diff > 20000 )
				i.remove();
		}

		CompassRequest r = new CompassRequest( attunement, x, y, z );
		CompassResult res = requests.get( r );

		if ( res == null )
		{
			res = new CompassResult( false, true, 0 );
			requests.put( r, res );
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

	private void requestUpdate(CompassRequest r)
	{

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
