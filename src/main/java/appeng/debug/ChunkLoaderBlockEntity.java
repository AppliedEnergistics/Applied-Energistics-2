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

package appeng.debug;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import appeng.core.AELog;
import appeng.tile.AEBaseBlockEntity;

public class ChunkLoaderBlockEntity extends AEBaseBlockEntity implements BlockEntityTicker {

    public ChunkLoaderBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    @Override
    public void onReady() {
        if (world.isClient) {
            return;
        }

        World world = getWorld();
        if (world instanceof ServerWorld) {
            ChunkPos chunkPos = new ChunkPos(getPos());
            ((ServerWorld) world).setChunkForced(chunkPos.x, chunkPos.z, true);
        }
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        World world = getWorld();
        if (world instanceof ServerWorld) {
            ChunkPos chunkPos = new ChunkPos(getPos());
            ((ServerWorld) world).setChunkForced(chunkPos.x, chunkPos.z, false);
        }
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        // Validate the force-status
        if (world instanceof ServerWorld) {
            ChunkPos chunkPos = new ChunkPos(getPos());
            ServerWorld serverWorld = (ServerWorld) world;

            if (!serverWorld.getForcedChunks().contains(chunkPos.toLong())) {
                AELog.debug("Force-loading chunk @ %d,%d in world %s", chunkPos.x, chunkPos.z,
                        serverWorld.getRegistryKey().getValue());
                serverWorld.setChunkForced(chunkPos.x, chunkPos.z, true);
            }
        }
    }
}
