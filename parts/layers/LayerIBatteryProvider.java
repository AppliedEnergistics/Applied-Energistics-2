package appeng.api.parts.layers;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import buildcraft.api.mj.IBatteryObject;
import buildcraft.api.mj.ISidedBatteryProvider;

public class LayerIBatteryProvider extends LayerBase implements ISidedBatteryProvider
{

	@Override
	public IBatteryObject getMjBattery(String kind, ForgeDirection direction)
	{
		IPart p = getPart( direction );

		if ( p instanceof ISidedBatteryProvider )
			return ((ISidedBatteryProvider) p).getMjBattery( kind, direction );

		return null;
	}

}
