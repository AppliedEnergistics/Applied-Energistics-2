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

	final public DimensionalCoord min;
	final public DimensionalCoord max;
	final List<TileSpatialPylon> line = new ArrayList<TileSpatialPylon>();
	public boolean isDestroyed = false;

	public Axis currentAxis = Axis.UNFORMED;
	public boolean isValid;
	public boolean hasPower;
	public boolean hasChannel;

	public SpatialPylonCluster( DimensionalCoord _min, DimensionalCoord _max )
	{
		this.min = _min.copy();
		this.max = _max.copy();

		if( this.min.x != this.max.x )
			this.currentAxis = Axis.X;
		else if( this.min.y != this.max.y )
			this.currentAxis = Axis.Y;
		else if( this.min.z != this.max.z )
			this.currentAxis = Axis.Z;
		else
			this.currentAxis = Axis.UNFORMED;
	}

	@Override
	public void updateStatus( boolean updateGrid )
	{
		for( TileSpatialPylon r : this.line )
		{
			r.recalculateDisplay();
		}
	}

	@Override
	public void destroy()
	{

		if( this.isDestroyed )
			return;
		this.isDestroyed = true;

		for( TileSpatialPylon r : this.line )
		{
			r.updateStatus( null );
		}
	}

	@Override
	public Iterator<IGridHost> getTiles()
	{
		return (Iterator) this.line.iterator();
	}

	public int tileCount()
	{
		return this.line.size();
	}

	public enum Axis
	{
		X, Y, Z, UNFORMED
	}
}
