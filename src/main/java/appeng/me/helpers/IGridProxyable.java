package appeng.me.helpers;

import appeng.api.networking.IGridHost;
import appeng.api.util.DimensionalCoord;

public interface IGridProxyable extends IGridHost
{

	AENetworkProxy getProxy();

	DimensionalCoord getLocation();

	void gridChanged();
}
