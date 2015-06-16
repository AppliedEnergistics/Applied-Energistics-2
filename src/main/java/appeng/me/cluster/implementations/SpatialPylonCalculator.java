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


import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.WorldCoord;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.MBCalculator;
import appeng.tile.spatial.TileSpatialPylon;


public class SpatialPylonCalculator extends MBCalculator
{

	private final TileSpatialPylon tqb;

	public SpatialPylonCalculator( IAEMultiBlock t )
	{
		super( t );
		this.tqb = (TileSpatialPylon) t;
	}

	@Override
	public boolean checkMultiblockScale( WorldCoord min, WorldCoord max )
	{
		return ( min.x == max.x && min.y == max.y && min.z != max.z ) || ( min.x == max.x && min.y != max.y && min.z == max.z ) || ( min.x != max.x && min.y == max.y && min.z == max.z );
	}

	@Override
	public IAECluster createCluster( World w, WorldCoord min, WorldCoord max )
	{
		return new SpatialPylonCluster( new DimensionalCoord( w, min.x, min.y, min.z ), new DimensionalCoord( w, max.x, max.y, max.z ) );
	}

	@Override
	public boolean verifyInternalStructure( World w, WorldCoord min, WorldCoord max )
	{

		for( int x = min.x; x <= max.x; x++ )
		{
			for( int y = min.y; y <= max.y; y++ )
			{
				for( int z = min.z; z <= max.z; z++ )
				{
					IAEMultiBlock te = (IAEMultiBlock) w.getTileEntity( new BlockPos( x, y, z ) );

					if( !te.isValid() )
					{
						return false;
					}
				}
			}
		}

		return true;
	}

	@Override
	public void disconnect()
	{
		this.tqb.disconnect( true );
	}

	@Override
	public void updateTiles( IAECluster cl, World w, WorldCoord min, WorldCoord max )
	{
		SpatialPylonCluster c = (SpatialPylonCluster) cl;

		for( int x = min.x; x <= max.x; x++ )
		{
			for( int y = min.y; y <= max.y; y++ )
			{
				for( int z = min.z; z <= max.z; z++ )
				{
					TileSpatialPylon te = (TileSpatialPylon) w.getTileEntity( new BlockPos( x, y, z ) );
					te.updateStatus( c );
					c.line.add( ( te ) );
				}
			}
		}
	}

	@Override
	public boolean isValidTile( TileEntity te )
	{
		return te instanceof TileSpatialPylon;
	}
}
