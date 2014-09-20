package appeng.me.cache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.spatial.ISpatialCache;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IReadOnlyCollection;
import appeng.core.AEConfig;
import appeng.me.cluster.implementations.SpatialPylonCluster;
import appeng.tile.spatial.TileSpatialIOPort;
import appeng.tile.spatial.TileSpatialPylon;

public class SpatialPylonCache implements IGridCache, ISpatialCache
{

	long powerRequired = 0;
	double efficiency = 0.0;

	DimensionalCoord captureMin;
	DimensionalCoord captureMax;
	boolean isValid = false;

	List<TileSpatialIOPort> ioPorts = new LinkedList();
	HashMap<SpatialPylonCluster, SpatialPylonCluster> clusters = new HashMap();

	boolean needsUpdate = false;

	final IGrid myGrid;

	public SpatialPylonCache(IGrid g) {
		myGrid = g;
	}

	@Override
	public long requiredPower()
	{
		return powerRequired;
	}

	@Override
	public boolean hasRegion()
	{
		return captureMin != null;
	}

	@Override
	public boolean isValidRegion()
	{
		return hasRegion() && isValid;
	}

	@Override
	public DimensionalCoord getMin()
	{
		return captureMin;
	}

	@Override
	public DimensionalCoord getMax()
	{
		return captureMax;
	}

	public void reset(IGrid grid)
	{
		int reqX = 0;
		int reqY = 0;
		int reqZ = 0;
		int requirePylongBlocks = 1;

		double minPower = 0;
		double maxPower = 0;

		clusters = new HashMap();
		ioPorts = new LinkedList();

		for (IGridNode gm : grid.getMachines( TileSpatialIOPort.class ))
		{
			ioPorts.add( (TileSpatialIOPort) gm.getMachine() );
		}

		IReadOnlyCollection<IGridNode> set = grid.getMachines( TileSpatialPylon.class );
		for (IGridNode gm : set)
		{
			if ( gm.meetsChannelRequirements() )
			{
				SpatialPylonCluster c = ((TileSpatialPylon) gm.getMachine()).getCluster();
				if ( c != null )
					clusters.put( c, c );
			}
		}

		captureMax = null;
		captureMin = null;
		isValid = true;

		int pylonBlocks = 0;
		for (SpatialPylonCluster cl : clusters.values())
		{
			if ( captureMax == null )
				captureMax = cl.max.copy();
			if ( captureMin == null )
				captureMin = cl.min.copy();

			pylonBlocks += cl.tileCount();

			captureMin.x = Math.min( captureMin.x, cl.min.x );
			captureMin.y = Math.min( captureMin.y, cl.min.y );
			captureMin.z = Math.min( captureMin.z, cl.min.z );

			captureMax.x = Math.max( captureMax.x, cl.max.x );
			captureMax.y = Math.max( captureMax.y, cl.max.y );
			captureMax.z = Math.max( captureMax.z, cl.max.z );
		}

		if ( hasRegion() )
		{
			isValid = captureMax.x - captureMin.x > 1 && captureMax.y - captureMin.y > 1 && captureMax.z - captureMin.z > 1;

			for (SpatialPylonCluster cl : clusters.values())
			{
				switch (cl.currentAxis)
				{
				case X:

					isValid = isValid && ((captureMax.y == cl.min.y || captureMin.y == cl.max.y) || (captureMax.z == cl.min.z || captureMin.z == cl.max.z))
							&& ((captureMax.y == cl.max.y || captureMin.y == cl.min.y) || (captureMax.z == cl.max.z || captureMin.z == cl.min.z));

					break;
				case Y:

					isValid = isValid && ((captureMax.x == cl.min.x || captureMin.x == cl.max.x) || (captureMax.z == cl.min.z || captureMin.z == cl.max.z))
							&& ((captureMax.x == cl.max.x || captureMin.x == cl.min.x) || (captureMax.z == cl.max.z || captureMin.z == cl.min.z));

					break;
				case Z:

					isValid = isValid && ((captureMax.y == cl.min.y || captureMin.y == cl.max.y) || (captureMax.x == cl.min.x || captureMin.x == cl.max.x))
							&& ((captureMax.y == cl.max.y || captureMin.y == cl.min.y) || (captureMax.x == cl.max.x || captureMin.x == cl.min.x));

					break;
				case UNFORMED:
					isValid = false;
					break;
				}
			}

			reqX = captureMax.x - captureMin.x;
			reqY = captureMax.y - captureMin.y;
			reqZ = captureMax.z - captureMin.z;
			requirePylongBlocks = Math.max( 6, ((reqX * reqZ + reqX * reqY + reqY * reqZ) * 3) / 8 );
			
			efficiency = (double) pylonBlocks / (double) requirePylongBlocks;
			
			if ( efficiency > 1.0 )
				efficiency = 1.0;
			if ( efficiency < 0.0 )
				efficiency = 0.0;
			
			minPower = (double) reqX * (double) reqY * reqZ * AEConfig.instance.spatialPowerMultiplier;
			maxPower = Math.pow( minPower, AEConfig.instance.spatialPowerScaler );
		}

		double affective_efficiency = Math.pow( efficiency, 0.25 );
		powerRequired = (long) (affective_efficiency * minPower + (1.0 - affective_efficiency) * maxPower);

		for (SpatialPylonCluster cl : clusters.values())
		{
			boolean myWasValid = cl.isValid;
			cl.isValid = isValid;
			if ( myWasValid != isValid )
				cl.updateStatus( false );
		}
	}

	@Override
	public float currentEfficiency()
	{
		return (float) efficiency * 100;
	}

	@MENetworkEventSubscribe
	public void bootingRender(MENetworkBootingStatusChange c)
	{
		reset( myGrid );
	}

	@Override
	public void onUpdateTick()
	{
	}

	@Override
	public void addNode(IGridNode node, IGridHost machine)
	{

	}

	@Override
	public void removeNode(IGridNode node, IGridHost machine)
	{

	}

	@Override
	public void onSplit(IGridStorage storageB)
	{

	}

	@Override
	public void onJoin(IGridStorage storageB)
	{

	}

	@Override
	public void populateGridStorage(IGridStorage storage)
	{

	}

}
