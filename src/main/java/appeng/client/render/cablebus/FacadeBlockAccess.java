/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.client.render.cablebus;


import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;


/**
 * This is used to retrieve the ExtendedState of a block for facade rendering.
 * It fakes the block at BlockPos provided as the BlockState provided.
 *
 * @author covers1624
 */
public class FacadeBlockAccess implements IBlockReader
{

	private final IBlockReader world;
	private final BlockPos pos;
	private final Direction side;
	private final BlockState state;

	public FacadeBlockAccess( IBlockReader world, BlockPos pos, Direction side, BlockState state )
	{
		this.world = world;
		this.pos = pos;
		this.side = side;
		this.state = state;
	}

	@Nullable
	@Override
	public TileEntity getTileEntity( BlockPos pos )
	{
		return this.world.getTileEntity( pos );
	}

	@Override
	public int getCombinedLight( BlockPos pos, int lightValue )
	{
		return this.world.getCombinedLight( pos, lightValue );
	}

	@Override
	public BlockState getBlockState( BlockPos pos )
	{
		if( this.pos == pos )
		{
			return this.state;
		}
		return this.world.getBlockState( pos );
	}

	@Override
	public boolean isAirBlock( BlockPos pos )
	{
		BlockState state = this.getBlockState( pos );
		return state.getBlock().isAir( state, this.world, pos );
	}

	@Override
	public Biome getBiome( BlockPos pos )
	{
		return this.world.getBiome( pos );
	}

	@Override
	public int getStrongPower( BlockPos pos, Direction direction )
	{
		return this.world.getStrongPower( pos, direction );
	}

	@Override
	public WorldType getWorldType()
	{
		return this.world.getWorldType();
	}

	@Override
	public boolean isSideSolid( BlockPos pos, Direction side, boolean _default )
	{
		if( pos.getX() < -30000000 || pos.getZ() < -30000000 || pos.getX() >= 30000000 || pos.getZ() >= 30000000 )
		{
			return _default;
		}
		else
		{
			return this.getBlockState( pos ).isSideSolid( this, pos, side );
		}
	}
}
