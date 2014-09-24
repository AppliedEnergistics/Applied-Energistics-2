package appeng.me.cluster;

import java.util.Iterator;

import appeng.api.networking.IGridHost;

public interface IAECluster
{

	void updateStatus(boolean updateGrid);

	void destroy();

	Iterator<IGridHost> getTiles();

}
