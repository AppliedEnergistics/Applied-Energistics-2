package appeng.api.parts.layers;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import buildcraft.api.power.IPowerEmitter;

public class LayerIPowerEmitter extends LayerBase implements IPowerEmitter
{

	@Override
	public boolean canEmitPowerFrom(ForgeDirection side)
	{
		IPart part = getPart( side );
		if ( part instanceof IPowerEmitter )
			return ((IPowerEmitter) part).canEmitPowerFrom( side );
		return false;
	}
}
