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

package appeng.me.cluster;


import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.util.WorldCoord;
import appeng.core.AELog;
import appeng.util.Platform;


public abstract class MBCalculator
{

	private final IAEMultiBlock target;

	public MBCalculator( IAEMultiBlock t )
	{
		this.target = t;
	}

	public void calculateMultiblock( World world, WorldCoord loc )
	{
		if( Platform.isClient() )
		{
			return;
		}

		try
		{
			WorldCoord min = loc.copy();
			WorldCoord max = loc.copy();

			// find size of MB structure...
			while( this.isValidTileAt( world, min.x - 1, min.y, min.z ) )
			{
				min.x--;
			}
			while( this.isValidTileAt( world, min.x, min.y - 1, min.z ) )
			{
				min.y--;
			}
			while( this.isValidTileAt( world, min.x, min.y, min.z - 1 ) )
			{
				min.z--;
			}
			while( this.isValidTileAt( world, max.x + 1, max.y, max.z ) )
			{
				max.x++;
			}
			while( this.isValidTileAt( world, max.x, max.y + 1, max.z ) )
			{
				max.y++;
			}
			while( this.isValidTileAt( world, max.x, max.y, max.z + 1 ) )
			{
				max.z++;
			}

			if( this.checkMultiblockScale( min, max ) )
			{
				if( this.verifyUnownedRegion( world, min, max ) )
				{
					IAECluster c = this.createCluster( world, min, max );

					try
					{
						if( !this.verifyInternalStructure( world, min, max ) )
						{
							this.disconnect();
							return;
						}
					}
					catch( Exception err )
					{
						this.disconnect();
						return;
					}

					boolean updateGrid = false;
					IAECluster cluster = this.target.getCluster();
					if( cluster == null )
					{
						this.updateTiles( c, world, min, max );

						updateGrid = true;
					}
					else
					{
						c = cluster;
					}

					c.updateStatus( updateGrid );
					return;
				}
			}
		}
		catch( Throwable err )
		{
			AELog.error( err );
		}

		this.disconnect();
	}

	public boolean isValidTileAt( World w, int x, int y, int z )
	{
		return this.isValidTile( w.getTileEntity( x, y, z ) );
	}

	/**
	 * verify if the structure is the correct dimensions, or size
	 *
	 * @param min min world coord
	 * @param max max world coord
	 *
	 * @return true if structure has correct dimensions or size
	 */
	public abstract boolean checkMultiblockScale( WorldCoord min, WorldCoord max );

	public boolean verifyUnownedRegion( World w, WorldCoord min, WorldCoord max )
	{
		for( ForgeDirection side : ForgeDirection.VALID_DIRECTIONS )
		{
			if( this.verifyUnownedRegionInner( w, min.x, min.y, min.z, max.x, max.y, max.z, side ) )
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * construct the correct cluster, usually very simple.
	 *
	 * @param w world
	 * @param min min world coord
	 * @param max max world coord
	 *
	 * @return created cluster
	 */
	public abstract IAECluster createCluster( World w, WorldCoord min, WorldCoord max );

	public abstract boolean verifyInternalStructure( World worldObj, WorldCoord min, WorldCoord max );

	/**
	 * disassembles the multi-block.
	 */
	public abstract void disconnect();

	/**
	 * configure the multi-block tiles, most of the important stuff is in here.
	 *
	 * @param c updated cluster
	 * @param w in world
	 * @param min min world coord
	 * @param max max world coord
	 */
	public abstract void updateTiles( IAECluster c, World w, WorldCoord min, WorldCoord max );

	/**
	 * check if the tile entities are correct for the structure.
	 *
	 * @param te to be checked tile entity
	 *
	 * @return true if tile entity is valid for structure
	 */
	public abstract boolean isValidTile( TileEntity te );

	public boolean verifyUnownedRegionInner( World w, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, ForgeDirection side )
	{
		switch( side )
		{
			case WEST:
				minX -= 1;
				maxX = minX;
				break;
			case EAST:
				maxX += 1;
				minX = maxX;
				break;
			case DOWN:
				minY -= 1;
				maxY = minY;
				break;
			case NORTH:
				maxZ += 1;
				minZ = maxZ;
				break;
			case SOUTH:
				minZ -= 1;
				maxZ = minZ;
				break;
			case UP:
				maxY += 1;
				minY = maxY;
				break;
			case UNKNOWN:
				return false;
		}

		for( int x = minX; x <= maxX; x++ )
		{
			for( int y = minY; y <= maxY; y++ )
			{
				for( int z = minZ; z <= maxZ; z++ )
				{
					TileEntity te = w.getTileEntity( x, y, z );
					if( this.isValidTile( te ) )
					{
						return true;
					}
				}
			}
		}

		return false;
	}
}
