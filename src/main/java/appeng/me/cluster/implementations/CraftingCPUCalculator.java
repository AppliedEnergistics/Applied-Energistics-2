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


import java.util.Iterator;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCraftingCpuChange;
import appeng.api.util.AEPartLocation;
import appeng.api.util.WorldCoord;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.MBCalculator;
import appeng.tile.crafting.TileCraftingTile;


public class CraftingCPUCalculator extends MBCalculator
{

	final TileCraftingTile tqb;

	public CraftingCPUCalculator( IAEMultiBlock t )
	{
		super( t );
		this.tqb = (TileCraftingTile) t;
	}

	@Override
	public boolean checkMultiblockScale( WorldCoord min, WorldCoord max )
	{
		if( max.x - min.x > 16 )
		{
			return false;
		}

		if( max.y - min.y > 16 )
		{
			return false;
		}

		if( max.z - min.z > 16 )
		{
			return false;
		}

		return true;
	}

	@Override
	public IAECluster createCluster( World w, WorldCoord min, WorldCoord max )
	{
		return new CraftingCPUCluster( min, max );
	}

	@Override
	public boolean verifyInternalStructure( World w, WorldCoord min, WorldCoord max )
	{
		boolean storage = false;

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

					if( !storage && te instanceof TileCraftingTile )
					{
						storage = ( (TileCraftingTile) te ).getStorageBytes() > 0;
					}
				}
			}
		}

		return storage;
	}

	@Override
	public void disconnect()
	{
		this.tqb.disconnect( true );
	}

	@Override
	public void updateTiles( IAECluster cl, World w, WorldCoord min, WorldCoord max )
	{
		CraftingCPUCluster c = (CraftingCPUCluster) cl;

		for( int x = min.x; x <= max.x; x++ )
		{
			for( int y = min.y; y <= max.y; y++ )
			{
				for( int z = min.z; z <= max.z; z++ )
				{
					TileCraftingTile te = (TileCraftingTile) w.getTileEntity( new BlockPos( x, y, z ) );
					te.updateStatus( c );
					c.addTile( te );
				}
			}
		}

		c.done();

		Iterator<IGridHost> i = c.getTiles();
		while( i.hasNext() )
		{
			IGridHost gh = i.next();
			IGridNode n = gh.getGridNode( AEPartLocation.INTERNAL );
			if( n != null )
			{
				IGrid g = n.getGrid();
				if( g != null )
				{
					g.postEvent( new MENetworkCraftingCpuChange( n ) );
					return;
				}
			}
		}
	}

	@Override
	public boolean isValidTile( TileEntity te )
	{
		return te instanceof TileCraftingTile;
	}
}
