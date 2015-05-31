/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.worldgen.meteorite;


import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import appeng.util.Platform;


public final class ChunkOnly extends StandardWorld
{

	final Chunk target;
	final int cx;
	final int cz;
	int verticalBits = 0;

	public ChunkOnly( World w, int cx, int cz )
	{
		super( w );
		this.target = w.getChunkFromChunkCoords( cx, cz );
		this.cx = cx;
		this.cz = cz;
	}

	@Override
	public int minX( int in )
	{
		return Math.max( in, this.cx << 4 );
	}

	@Override
	public int minZ( int in )
	{
		return Math.max( in, this.cz << 4 );
	}

	@Override
	public int maxX( int in )
	{
		return Math.min( in, ( this.cx + 1 ) << 4 );
	}

	@Override
	public int maxZ( int in )
	{
		return Math.min( in, ( this.cz + 1 ) << 4 );
	}

	@Override
	public int getBlockMetadata( int x, int y, int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.target.getBlockMetadata( x & 0xF, y, z & 0xF );
		}
		return 0;
	}

	@Override
	public Block getBlock( int x, int y, int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.target.getBlock( x & 0xF, y, z & 0xF );
		}
		return Platform.AIR_BLOCK;
	}

	@Override
	public void setBlock( int x, int y, int z, Block blk )
	{
		if( this.range( x, y, z ) )
		{
			this.verticalBits |= 1 << ( y >> 4 );
			this.w.setBlock( x, y, z, blk, 0, 1 );
		}
	}

	@Override
	public void setBlock( int x, int y, int z, Block block, int meta, int flags )
	{
		if( this.range( x, y, z ) )
		{
			this.verticalBits |= 1 << ( y >> 4 );
			this.w.setBlock( x, y, z, block, meta, flags & ( ~2 ) );
		}
	}

	@Override
	public void done()
	{
		if( this.verticalBits != 0 )
		{
			Platform.sendChunk( this.target, this.verticalBits );
		}
	}

	@Override
	public final boolean range( int x, int y, int z )
	{
		return this.cx == ( x >> 4 ) && this.cz == ( z >> 4 );
	}
}
