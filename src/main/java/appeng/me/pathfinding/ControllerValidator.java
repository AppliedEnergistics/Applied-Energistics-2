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

package appeng.me.pathfinding;


import net.minecraft.util.BlockPos;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridVisitor;
import appeng.tile.networking.TileController;


public class ControllerValidator implements IGridVisitor
{

	public boolean isValid = true;
	public int found = 0;
	int minX;
	int minY;
	int minZ;
	int maxX;
	int maxY;
	int maxZ;

	public ControllerValidator( int x, int y, int z )
	{
		this.minX = x;
		this.maxX = x;
		this.minY = y;
		this.maxY = y;
		this.minZ = z;
		this.maxZ = z;
	}

	@Override
	public boolean visitNode( IGridNode n )
	{
		IGridHost host = n.getMachine();
		if( this.isValid && host instanceof TileController )
		{
			TileController c = (TileController) host;

			BlockPos pos = c.getPos();
			
			this.minX = Math.min( pos.getX(), this.minX );
			this.maxX = Math.max( pos.getX(), this.maxX );
			this.minY = Math.min( pos.getY(), this.minY );
			this.maxY = Math.max( pos.getY(), this.maxY );
			this.minZ = Math.min( pos.getZ(), this.minZ );
			this.maxZ = Math.max( pos.getZ(), this.maxZ );

			if( this.maxX - this.minX < 7 && this.maxY - this.minY < 7 && this.maxZ - this.minZ < 7 )
			{
				this.found++;
				return true;
			}

			this.isValid = false;
		}
		else
		{
			return false;
		}

		return this.isValid;
	}
}
