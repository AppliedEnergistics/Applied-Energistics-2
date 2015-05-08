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

package appeng.util;


import java.util.concurrent.Callable;

import net.minecraft.world.World;


public class BlockUpdate implements Callable
{

	final World w;
	final int x;
	final int y;
	final int z;

	public BlockUpdate( World w, int x, int y, int z )
	{
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public Object call() throws Exception
	{
		if( this.w.blockExists( this.x, this.y, this.z ) )
		{
			this.w.notifyBlocksOfNeighborChange( this.x, this.y, this.z, Platform.AIR_BLOCK );
		}

		return true;
	}
}
