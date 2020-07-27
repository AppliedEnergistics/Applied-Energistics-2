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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import appeng.util.LookDirection;
import appeng.util.Platform;

/**
 * Tracer for players hitting blocks
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class Tracer {
    /**
     * Trace view of players to blocks. Ignore all which are out of reach.
     *
     * @param world  word of block
     * @param player player viewing block
     * @param pos    pos of block
     *
     * @return trace movement. Can be null
     */
    public HitResult retraceBlock(final World world, final PlayerEntity player, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);

        LookDirection playerRay = Platform.getPlayerRay(player);
        return blockState.getCollisionShape(world, pos).rayTrace(playerRay.getA(), playerRay.getB(), pos);
    }
}
