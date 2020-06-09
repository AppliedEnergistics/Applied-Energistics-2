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

package appeng.integration.modules.waila.part;


import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;


/**
 * Tracer for players hitting blocks
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class Tracer
{
	/**
	 * Trace view of players to blocks.
	 * Ignore all which are out of reach.
	 *
	 * @param world word of block
	 * @param player player viewing block
	 * @param pos pos of block
	 *
	 * @return trace movement. Can be null
	 */
	public RayTraceResult retraceBlock(final World world, final PlayerEntity player, BlockPos pos )
	{
		BlockState blockState = world.getBlockState( pos );

		final Vec3d headVec = this.getCorrectedHeadVec( player );
		final Vec3d lookVec = player.getLook( 1.0F );
		final double reach = this.getBlockReachDistance_server( player );
		final Vec3d endVec = headVec.add( lookVec.x * reach, lookVec.y * reach, lookVec.z * reach );

		return blockState.getCollisionShape(world, pos).rayTrace( headVec, endVec, pos );
	}

	/**
	 * Gets the view point of a player
	 *
	 * @param player player with head
	 *
	 * @return view point of player
	 */
	private Vec3d getCorrectedHeadVec( final PlayerEntity player )
	{
		double x = player.getPosX();
		double y = player.getPosY();
		double z = player.getPosZ();

		if( player.world.isRemote )
		{
			// compatibility with eye height changing mods
			y += player.getEyeHeight() - player.getEyeHeight();
		}
		else
		{
			y += player.getEyeHeight();
			if( player instanceof ServerPlayerEntity && player.isSneaking() )
			{
				y -= 0.08;
			}
		}
		return new Vec3d( x, y, z );
	}

	/**
	 * @param player multi-player player
	 *
	 * @return block reach distance of player
	 */
	private double getBlockReachDistance_server( final PlayerEntity player )
	{
		return player.getAttribute(net.minecraft.entity.player.PlayerEntity.REACH_DISTANCE).getValue();
	}
}
