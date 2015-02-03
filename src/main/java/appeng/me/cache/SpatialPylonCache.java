/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.me.cache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import appeng.api.networking.IGrid;
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

public class SpatialPylonCache implements ISpatialCache
{

	long powerRequired = 0;
	double efficiency = 0.0;

	DimensionalCoord captureMin;
	DimensionalCoord captureMax;
	boolean isValid = false;

	List<TileSpatialIOPort> ioPorts = new LinkedList<TileSpatialIOPort>();
	HashMap<SpatialPylonCluster, SpatialPylonCluster> clusters = new HashMap<SpatialPylonCluster, SpatialPylonCluster>();

	boolean needsUpdate = false;

	final IGrid myGrid;

	public SpatialPylonCache(IGrid g) {
		this.myGrid = g;
	}

	@Override
	public long requiredPower()
	{
		return this.powerRequired;
	}

	@Override
	public boolean hasRegion()
	{
		return this.captureMin != null;
	}

	@Override
	public boolean isValidRegion()
	{
		return this.hasRegion() && this.isValid;
	}

	@Override
	public DimensionalCoord getMin()
	{
		return this.captureMin;
	}

	@Override
	public DimensionalCoord getMax()
	{
		return this.captureMax;
	}

	public void reset(IGrid grid)
	{
		int reqX = 0;
		int reqY = 0;
		int reqZ = 0;
		int requirePylonBlocks = 1;

		double minPower = 0;
		double maxPower = 0;

		this.clusters = new HashMap<SpatialPylonCluster, SpatialPylonCluster>();
		this.ioPorts = new LinkedList<TileSpatialIOPort>();

		for (IGridNode gm : grid.getMachines( TileSpatialIOPort.class ))
		{
			this.ioPorts.add( (TileSpatialIOPort) gm.getMachine() );
		}

		IReadOnlyCollection<IGridNode> set = grid.getMachines( TileSpatialPylon.class );
		for (IGridNode gm : set)
		{
			if ( gm.meetsChannelRequirements() )
			{
				SpatialPylonCluster c = ((TileSpatialPylon) gm.getMachine()).getCluster();
				if ( c != null )
					this.clusters.put( c, c );
			}
		}

		this.captureMax = null;
		this.captureMin = null;
		this.isValid = true;

		int pylonBlocks = 0;
		for (SpatialPylonCluster cl : this.clusters.values())
		{
			if ( this.captureMax == null )
				this.captureMax = cl.max.copy();
			if ( this.captureMin == null )
				this.captureMin = cl.min.copy();

			pylonBlocks += cl.tileCount();

			this.captureMin.x = Math.min( this.captureMin.x, cl.min.x );
			this.captureMin.y = Math.min( this.captureMin.y, cl.min.y );
			this.captureMin.z = Math.min( this.captureMin.z, cl.min.z );

			this.captureMax.x = Math.max( this.captureMax.x, cl.max.x );
			this.captureMax.y = Math.max( this.captureMax.y, cl.max.y );
			this.captureMax.z = Math.max( this.captureMax.z, cl.max.z );
		}

		if ( this.hasRegion() )
		{
			this.isValid = this.captureMax.x - this.captureMin.x > 1 && this.captureMax.y - this.captureMin.y > 1 && this.captureMax.z - this.captureMin.z > 1;

			for (SpatialPylonCluster cl : this.clusters.values())
			{
				switch (cl.currentAxis)
				{
				case X:

					this.isValid = this.isValid && ((this.captureMax.y == cl.min.y || this.captureMin.y == cl.max.y) || (this.captureMax.z == cl.min.z || this.captureMin.z == cl.max.z))
							&& ((this.captureMax.y == cl.max.y || this.captureMin.y == cl.min.y) || (this.captureMax.z == cl.max.z || this.captureMin.z == cl.min.z));

					break;
				case Y:

					this.isValid = this.isValid && ((this.captureMax.x == cl.min.x || this.captureMin.x == cl.max.x) || (this.captureMax.z == cl.min.z || this.captureMin.z == cl.max.z))
							&& ((this.captureMax.x == cl.max.x || this.captureMin.x == cl.min.x) || (this.captureMax.z == cl.max.z || this.captureMin.z == cl.min.z));

					break;
				case Z:

					this.isValid = this.isValid && ((this.captureMax.y == cl.min.y || this.captureMin.y == cl.max.y) || (this.captureMax.x == cl.min.x || this.captureMin.x == cl.max.x))
							&& ((this.captureMax.y == cl.max.y || this.captureMin.y == cl.min.y) || (this.captureMax.x == cl.max.x || this.captureMin.x == cl.min.x));

					break;
				case UNFORMED:
					this.isValid = false;
					break;
				}
			}

			reqX = this.captureMax.x - this.captureMin.x;
			reqY = this.captureMax.y - this.captureMin.y;
			reqZ = this.captureMax.z - this.captureMin.z;
			requirePylonBlocks = Math.max( 6, ((reqX * reqZ + reqX * reqY + reqY * reqZ) * 3) / 8 );

			this.efficiency = (double) pylonBlocks / (double) requirePylonBlocks;

			if ( this.efficiency > 1.0 )
				this.efficiency = 1.0;
			if ( this.efficiency < 0.0 )
				this.efficiency = 0.0;

			minPower = (double) reqX * (double) reqY * reqZ * AEConfig.instance.spatialPowerMultiplier;
			maxPower = Math.pow( minPower, AEConfig.instance.spatialPowerExponent );
		}

		double affective_efficiency = Math.pow( this.efficiency, 0.25 );
		this.powerRequired = (long) (affective_efficiency * minPower + (1.0 - affective_efficiency) * maxPower);

		for (SpatialPylonCluster cl : this.clusters.values())
		{
			boolean myWasValid = cl.isValid;
			cl.isValid = this.isValid;
			if ( myWasValid != this.isValid )
				cl.updateStatus( false );
		}
	}

	@Override
	public float currentEfficiency()
	{
		return (float) this.efficiency * 100;
	}

	@MENetworkEventSubscribe
	public void bootingRender(MENetworkBootingStatusChange c)
	{
		this.reset( this.myGrid );
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
