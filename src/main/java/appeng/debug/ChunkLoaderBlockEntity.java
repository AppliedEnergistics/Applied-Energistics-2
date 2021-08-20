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

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.core.AELog;

public class ChunkLoaderBlockEntity extends AEBaseBlockEntity implements ServerTickingBlockEntity {

    public ChunkLoaderBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();

        Level level = getLevel();
        if (level instanceof ServerLevel) {
            ChunkPos chunkPos = new ChunkPos(getBlockPos());
            ((ServerLevel) level).setChunkForced(chunkPos.x, chunkPos.z, true);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        Level level = getLevel();
        if (level instanceof ServerLevel) {
            ChunkPos chunkPos = new ChunkPos(getBlockPos());
            ((ServerLevel) level).setChunkForced(chunkPos.x, chunkPos.z, false);
        }
    }

    @Override
    public void serverTick() {
        // Validate the force-status
        var serverLevel = (ServerLevel) getLevel();
        ChunkPos chunkPos = new ChunkPos(getBlockPos());

        if (!serverLevel.getForcedChunks().contains(chunkPos.toLong())) {
            AELog.debug("Force-loading chunk @ %d,%d in %s", chunkPos.x, chunkPos.z, serverLevel.dimension());
            serverLevel.setChunkForced(chunkPos.x, chunkPos.z, false);
        }
    }
}
