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

	int minX;
	int minY;
	int minZ;

	int maxX;
	int maxY;
	int maxZ;

	public boolean isValid = true;
	public int found = 0;

	public ControllerValidator(int x, int y, int z) {
		minX = x;
		minY = y;
		minZ = z;
		maxX = x;
		maxY = y;
		maxZ = z;
	}

	@Override
	public boolean visitNode(IGridNode n)
	{
		IGridHost host = n.getMachine();
		if ( isValid && host instanceof TileController )
		{
			TileController c = (TileController) host;

			minX = Math.min( c.xCoord, minX );
			maxX = Math.max( c.xCoord, maxX );
			minY = Math.min( c.yCoord, minY );
			maxY = Math.max( c.yCoord, maxY );
			minZ = Math.min( c.zCoord, minZ );
			maxZ = Math.max( c.zCoord, maxZ );

			if ( maxX - minX < 7 && maxY - minY < 7 && maxZ - minZ < 7 )
			{
				found++;
				return true;
			}

			isValid = false;
		}
		else
			return false;

		return isValid;
	}
}
