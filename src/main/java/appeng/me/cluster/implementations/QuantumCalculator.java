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
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.util.WorldCoord;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.MBCalculator;
import appeng.tile.qnb.TileQuantumBridge;
import appeng.util.Platform;

public class QuantumCalculator extends MBCalculator
{

	final private TileQuantumBridge tqb;

	public QuantumCalculator(IAEMultiBlock t) {
		super( t );
		this.tqb = (TileQuantumBridge) t;
	}

	@Override
	public boolean isValidTile(TileEntity te)
	{
		return te instanceof TileQuantumBridge;
	}

	@Override
	public boolean checkMultiblockScale(WorldCoord min, WorldCoord max)
	{

		if ( (max.x - min.x + 1) * (max.y - min.y + 1) * (max.z - min.z + 1) == 9 )
		{
			int ones = ((max.x - min.x) == 0 ? 1 : 0) + ((max.y - min.y) == 0 ? 1 : 0) + ((max.z - min.z) == 0 ? 1 : 0);

			int threes = ((max.x - min.x) == 2 ? 1 : 0) + ((max.y - min.y) == 2 ? 1 : 0) + ((max.z - min.z) == 2 ? 1 : 0);

			return ones == 1 && threes == 2;
		}
		return false;
	}

	@Override
	public void updateTiles(IAECluster cl, World w, WorldCoord min, WorldCoord max)
	{
		byte num = 0;
		byte ringNum = 0;
		QuantumCluster c = (QuantumCluster) cl;

		for (int x = min.x; x <= max.x; x++)
		{
			for (int y = min.y; y <= max.y; y++)
			{
				for (int z = min.z; z <= max.z; z++)
				{
					TileQuantumBridge te = (TileQuantumBridge) w.getTileEntity( x, y, z );

					byte flags = 0;

					num++;
					if ( num == 5 )
					{
						flags = num;
						c.setCenter( te );
					}
					else
					{
						if ( num == 1 || num == 3 || num == 7 || num == 9 )
							flags = (byte) (this.tqb.corner | num);
						else
							flags = num;
						c.Ring[ringNum] = te;
						ringNum++;
					}

					te.updateStatus( c, flags, true );
				}
			}
		}

	}

	@Override
	public IAECluster createCluster(World w, WorldCoord min, WorldCoord max)
	{
		return new QuantumCluster( min, max );
	}

	@Override
	public void disconnect()
	{
		this.tqb.disconnect(true);
	}

	@Override
	public boolean verifyInternalStructure(World w, WorldCoord min, WorldCoord max)
	{

		byte num = 0;

		for (int x = min.x; x <= max.x; x++)
		{
			for (int y = min.y; y <= max.y; y++)
			{
				for (int z = min.z; z <= max.z; z++)
				{
					IAEMultiBlock te = (IAEMultiBlock) w.getTileEntity( x, y, z );

					if ( !te.isValid() )
						return false;

					num++;
					if ( num == 5 )
					{
						if ( !Platform.blockAtLocationIs( w, x, y, z, AEApi.instance().blocks().blockQuantumLink ) )
							return false;
					}
					else
					{
						if ( !Platform.blockAtLocationIs( w, x, y, z, AEApi.instance().blocks().blockQuantumRing ) )
							return false;
					}

				}
			}
		}
		return true;
	}

}
