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

package appeng.spatial;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import appeng.api.movable.IMovableHandler;

public class DefaultSpatialHandler implements IMovableHandler
{

	@Override
	public void moveTile(TileEntity te, World w, int x, int y, int z)
	{

		te.setWorldObj( w );
		te.xCoord = x;
		te.yCoord = y;
		te.zCoord = z;

		Chunk c = w.getChunkFromBlockCoords( x, z );
		c.func_150812_a( x & 0xF, y, z & 0xF, te );
		// c.setChunkBlockTileEntity( x & 0xF, y, z & 0xF, te );

		if ( c.isChunkLoaded )
		{
			w.addTileEntity( te );
			w.markBlockForUpdate( x, y, z );
		}
	}

	/**
	 * never called for the default.
	 * 
	 * @param tile tile entity
	 * @return true
	 */
	@Override
	public boolean canHandle(Class<? extends TileEntity> myClass, TileEntity tile)
	{
		return true;
	}

}
