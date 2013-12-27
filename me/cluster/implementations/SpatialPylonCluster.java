package appeng.me.cluster.implementations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import appeng.api.networking.IGridHost;
import appeng.api.util.DimensionalCoord;
import appeng.me.cluster.IAECluster;
import appeng.tile.spatial.TileSpatialPylon;

public class SpatialPylonCluster implements IAECluster
{

	public enum Axis
	{
		X, Y, Z, UNFORMED
	};

	public DimensionalCoord min;
	public DimensionalCoord max;
	public boolean isDestroyed = false;

	public Axis currentAxis = Axis.UNFORMED;

	List<TileSpatialPylon> line = new ArrayList();
	public boolean isValid;
	public boolean hasPower;
	public boolean hasChannel;

	public SpatialPylonCluster(DimensionalCoord _min, DimensionalCoord _max) {
		min = _min.copy();
		max = _max.copy();

		if ( min.x != max.x )
			currentAxis = Axis.X;
		else if ( min.y != max.y )
			currentAxis = Axis.Y;
		else if ( min.z != max.z )
			currentAxis = Axis.Z;
		else
			currentAxis = Axis.UNFORMED;
	}

	@Override
	public void updateStatus(boolean updateGrid)
	{
		for (TileSpatialPylon r : line)
		{
			r.recalculateDisplay();
		}
	}

	@Override
	public void destroy()
	{

		if ( isDestroyed )
			return;
		isDestroyed = true;

		for (TileSpatialPylon r : line)
		{
			r.updateStatus( null );
		}

	}

	public int tileCount()
	{
		return line.size();
	}

	@Override
	public Iterator<IGridHost> getTiles()
	{
		return (Iterator) line.iterator();
	}

}
