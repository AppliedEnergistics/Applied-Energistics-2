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


import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
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
	 * @param world  word of block
	 * @param player player viewing block
	 * @param x      x pos of block
	 * @param y      y pos of block
	 * @param z      z pos of block
	 * @return trace movement. Can be null
	 */
	public MovingObjectPosition retraceBlock( final World world, final EntityPlayerMP player, final int x, final int y, final int z )
	{
		final Block block = world.getBlock( x, y, z );

		final Vec3 headVec = this.getCorrectedHeadVec( player );
		final Vec3 lookVec = player.getLook( 1.0F );
		final double reach = this.getBlockReachDistance_server( player );
		final Vec3 endVec = headVec.addVector( lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach );

		return block.collisionRayTrace( world, x, y, z, headVec, endVec );
	}

	/**
	 * Gets the view point of a player
	 *
	 * @param player player with head
	 * @return view point of player
	 */
	private Vec3 getCorrectedHeadVec( final EntityPlayer player )
	{
		final Vec3 v = Vec3.createVectorHelper( player.posX, player.posY, player.posZ );
		if( player.worldObj.isRemote )
		{
			// compatibility with eye height changing mods
			v.yCoord += player.getEyeHeight() - player.getDefaultEyeHeight();
		}
		else
		{
			v.yCoord += player.getEyeHeight();
			if( player instanceof EntityPlayerMP && player.isSneaking() )
			{
				v.yCoord -= 0.08;
			}
		}
		return v;
	}

	/**
	 * @param player multi-player player
	 * @return block reach distance of player
	 */
	private double getBlockReachDistance_server( final EntityPlayerMP player )
	{
		return player.theItemInWorldManager.getBlockReachDistance();
	}
}
