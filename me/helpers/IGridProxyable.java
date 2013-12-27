package appeng.me.helpers;

import appeng.api.networking.IGridHost;
import appeng.api.util.DimensionalCoord;

public interface IGridProxyable extends IGridHost
{

	DimensionalCoord getLocation();

	void gridChanged();
}
