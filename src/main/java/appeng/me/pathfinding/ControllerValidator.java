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


import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridVisitor;
import appeng.tile.networking.TileController;


public class ControllerValidator implements IGridVisitor
{

	private boolean isValid = true;
	private int found = 0;
	private int minX;
	private int minY;
	private int minZ;
	private int maxX;
	private int maxY;
	private int maxZ;

	public ControllerValidator( final int x, final int y, final int z )
	{
		this.minX = x;
		this.minY = y;
		this.minZ = z;
		this.maxX = x;
		this.maxY = y;
		this.maxZ = z;
	}

	@Override
	public boolean visitNode( final IGridNode n )
	{
		final IGridHost host = n.getMachine();
		if( this.isValid() && host instanceof TileController )
		{
			final TileController c = (TileController) host;

			this.minX = Math.min( c.xCoord, this.minX );
			this.maxX = Math.max( c.xCoord, this.maxX );
			this.minY = Math.min( c.yCoord, this.minY );
			this.maxY = Math.max( c.yCoord, this.maxY );
			this.minZ = Math.min( c.zCoord, this.minZ );
			this.maxZ = Math.max( c.zCoord, this.maxZ );

			if( this.maxX - this.minX < 7 && this.maxY - this.minY < 7 && this.maxZ - this.minZ < 7 )
			{
				this.setFound( this.getFound() + 1 );
				return true;
			}

			this.setValid( false );
		}
		else
		{
			return false;
		}

		return this.isValid();
	}

	public boolean isValid()
	{
		return this.isValid;
	}

	private void setValid( final boolean isValid )
	{
		this.isValid = isValid;
	}

	public int getFound()
	{
		return this.found;
	}

	private void setFound( final int found )
	{
		this.found = found;
	}
}
