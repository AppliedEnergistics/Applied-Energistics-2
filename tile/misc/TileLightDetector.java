package appeng.tile.misc;

import appeng.tile.AEBaseTile;
import appeng.tile.events.AETileEventHandler;
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

	class LightDetectorHandler extends AETileEventHandler
	{

		public LightDetectorHandler() {
			super( TileEventType.TICK );
		}

		@Override
		public void Tick()
		{
			lastCheck++;
			if ( lastCheck > 30 )
			{
				lastCheck = 0;
				updateLight();
			}
		}

	};

	public TileLightDetector() {
		addNewHandler( new LightDetectorHandler() );
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
