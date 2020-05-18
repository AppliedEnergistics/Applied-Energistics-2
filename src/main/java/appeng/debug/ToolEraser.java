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

package appeng.debug;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.core.AELog;
import appeng.items.AEBaseItem;
import appeng.util.Platform;


public class ToolEraser extends AEBaseItem
{

	private static final int BLOCK_ERASE_LIMIT = 90000;

	@Override
	public EnumActionResult onItemUseFirst( final PlayerEntity player, final World world, final BlockPos pos, final Direction side, final float hitX, final float hitY, final float hitZ, final Hand hand )
	{
		if( Platform.isClient() )
		{
			return EnumActionResult.PASS;
		}

		final BlockState state = world.getBlockState( pos );

		List<BlockPos> next = new ArrayList<>();
		next.add( pos );

		int blocks = 0;
		while( blocks < BLOCK_ERASE_LIMIT && !next.isEmpty() )
		{
			final List<BlockPos> c = next;
			next = new ArrayList<>();

			for( final BlockPos wc : c )
			{
				final BlockState c_state = world.getBlockState( wc );

				if( state == c_state )
				{
					blocks++;
					world.setBlockToAir( wc );

					next.add( wc.add( 1, 0, 0 ) );
					next.add( wc.add( -1, 0, 0 ) );
					next.add( wc.add( 0, 1, 0 ) );
					next.add( wc.add( 0, -1, 0 ) );
					next.add( wc.add( 0, 0, 1 ) );
					next.add( wc.add( 0, 0, -1 ) );
				}
			}
		}

		AELog.info( "Delete " + blocks + " blocks" );

		return EnumActionResult.SUCCESS;
	}
}
