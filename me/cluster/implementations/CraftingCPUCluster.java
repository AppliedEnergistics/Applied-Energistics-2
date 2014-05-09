package appeng.me.cluster.implementations;

import java.util.Iterator;
import java.util.LinkedList;

import appeng.api.networking.IGridHost;
import appeng.api.util.WorldCoord;
import appeng.me.cluster.IAECluster;

public class CraftingCPUCluster implements IAECluster
{

	public WorldCoord min;
	public WorldCoord max;
	public boolean isDestroyed = false;

	private LinkedList<IGridHost> tiles = new LinkedList();

	@Override
	public Iterator<IGridHost> getTiles()
	{
		return tiles.iterator();
	}

	public CraftingCPUCluster(WorldCoord _min, WorldCoord _max) {
		min = _min;
		max = _max;
	}

	@Override
	public void updateStatus(boolean updateGrid)
	{

	}

	@Override
	public void destroy()
	{
		if ( isDestroyed )
			return;
		isDestroyed = true;

	}

}
