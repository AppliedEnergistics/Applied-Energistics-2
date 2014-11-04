package appeng.services.helpers;

import java.io.File;
import java.util.HashMap;


public class CompassReader
{
	private final HashMap<Long, CompassRegion> regions = new HashMap<Long, CompassRegion>();
	private final int dimensionId;
	private final File rootFolder;

	public void close()
	{
		for (CompassRegion r : this.regions.values())
		{
			r.close();
		}

		this.regions.clear();
	}

	public CompassReader(int dimensionId, File rootFolder)
	{
		this.dimensionId = dimensionId;
		this.rootFolder = rootFolder;
	}

	public void setHasBeacon(int cx, int cz, int cdy, boolean hasBeacon)
	{
		CompassRegion r = getRegion( cx, cz );
		r.setHasBeacon( cx, cz, cdy, hasBeacon );
	}

	public boolean hasBeacon(int cx, int cz)
	{
		CompassRegion r = getRegion( cx, cz );
		return r.hasBeacon( cx, cz );
	}

	private CompassRegion getRegion(int cx, int cz)
	{
		long pos = cx >> 10;
		pos = pos << 32;
		pos = pos | (cz >> 10);

		CompassRegion cr = this.regions.get( pos );
		if ( cr == null )
		{
			cr = new CompassRegion( cx, cz, this.dimensionId, this.rootFolder );
			this.regions.put( pos, cr );
		}

		return cr;

	}

}
