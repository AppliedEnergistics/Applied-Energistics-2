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
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import appeng.util.Platform;
import net.minecraft.world.chunk.IChunk;


public class ChunkOnly extends StandardWorld
{

	private final IChunk target;
	private final int cx;
	private final int cz;

	public ChunkOnly(final IWorld w, final int cx, final int cz )
	{
		super( w );
		this.target = w.getChunk( cx, cz );
		this.cx = cx;
		this.cz = cz;
	}

	@Override
	public int minX( final int in )
	{
		return Math.max( in, this.cx << 4 );
	}

	@Override
	public int minZ( final int in )
	{
		return Math.max( in, this.cz << 4 );
	}

	@Override
	public int maxX( final int in )
	{
		return Math.min( in, ( this.cx + 1 ) << 4 );
	}

	@Override
	public int maxZ( final int in )
	{
		return Math.min( in, ( this.cz + 1 ) << 4 );
	}

    @Override
    public boolean contains(BlockPos pos) {
        return this.range(pos);
    }

    @Override
	public Block getBlock( BlockPos pos )
	{
		if( this.range( pos ) )
		{
			return this.target.getBlockState( new BlockPos(pos) ).getBlock();
		}
		return Platform.AIR_BLOCK;
	}

	@Override
	public void setBlock( BlockPos pos, final BlockState blk )
	{
		if( this.range( pos ) )
		{
			target.setBlockState(new BlockPos( pos ), blk, false);
		}
	}

	@Override
	public void setBlock( BlockPos pos, final BlockState state, final int flags )
	{
		if( this.range( pos ) )
		{
			this.getWorld().setBlockState( new BlockPos( pos ), state, flags & ( ~2 ) );
		}
	}

	@Override
	public boolean range( BlockPos pos )
	{
		return this.cx == ( pos.getX() >> 4 ) && this.cz == ( pos.getZ() >> 4 );
	}
}
