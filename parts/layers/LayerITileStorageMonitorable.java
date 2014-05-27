package appeng.api.parts.layers;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import appeng.api.storage.IStorageMonitorable;

public class LayerITileStorageMonitorable extends LayerBase implements ITileStorageMonitorable
{

	@Override
	public IStorageMonitorable getMonitorable(ForgeDirection side, BaseActionSource src)
	{
		IPart part = getPart( side );
		if ( part instanceof ITileStorageMonitorable )
			return ((ITileStorageMonitorable) part).getMonitorable( side, src );
		return null;
	}

}
