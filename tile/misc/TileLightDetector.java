package appeng.tile.misc;

import appeng.tile.AEBaseTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.util.Platform;

public class TileLightDetector extends AEBaseTile
{

	int lastCheck = 30;
	int lastLight = 0;

	public boolean isReady()
	{
		return lastLight > 0;
	}

	@TileEvent(TileEventType.TICK)
	public void Tick_TileLightDetector()
	{
		lastCheck++;
		if ( lastCheck > 30 )
		{
			lastCheck = 0;
			updateLight();
		}
	}

	public void updateLight()
	{
		int val = worldObj.getBlockLightValue( xCoord, yCoord, zCoord );

		if ( lastLight != val )
		{
			lastLight = val;
			Platform.notifyBlocksOfNeighbors( worldObj, xCoord, yCoord, zCoord );
		}
	}

	@Override
	public boolean canBeRotated()
	{
		return false;
	}

}
