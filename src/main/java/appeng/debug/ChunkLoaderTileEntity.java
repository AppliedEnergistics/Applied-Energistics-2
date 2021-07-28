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

import appeng.core.AELog;
import appeng.tile.AEBaseTileEntity;
import appeng.tile.ServerTickingBlockEntity;

public class ChunkLoaderTileEntity extends AEBaseTileEntity implements ServerTickingBlockEntity {

    public ChunkLoaderTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState blockState) {
        super(tileEntityTypeIn, pos, blockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        Level world = getLevel();
        if (world instanceof ServerLevel) {
            ChunkPos chunkPos = new ChunkPos(getBlockPos());
            ((ServerLevel) world).setChunkForced(chunkPos.x, chunkPos.z, true);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        Level world = getLevel();
        if (world instanceof ServerLevel) {
            ChunkPos chunkPos = new ChunkPos(getBlockPos());
            ((ServerLevel) world).setChunkForced(chunkPos.x, chunkPos.z, false);
        }
    }

    @Override
    public void serverTick() {
        // Validate the force-status
        var serverWorld = (ServerLevel) getLevel();
        ChunkPos chunkPos = new ChunkPos(getBlockPos());

        if (!serverWorld.getForcedChunks().contains(chunkPos.toLong())) {
            AELog.debug("Force-loading chunk @ %d,%d in %s", chunkPos.x, chunkPos.z, serverWorld.dimension());
            serverWorld.setChunkForced(chunkPos.x, chunkPos.z, false);
        }
    }
}
