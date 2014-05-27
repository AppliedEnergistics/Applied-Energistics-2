package appeng.api.parts.layers;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile.PipeType;

public class LayerIPipeConnection extends LayerBase implements IPipeConnection
{

	@Override
	public ConnectOverride overridePipeConnection(PipeType type, ForgeDirection with)
	{
		IPart part = getPart( with );
		if ( part instanceof IPipeConnection )
			return ((IPipeConnection) part).overridePipeConnection( type, with );
		return ConnectOverride.DEFAULT;
	}

}
