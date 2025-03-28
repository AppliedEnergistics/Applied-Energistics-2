/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.server.services;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.world.chunk.LoadingValidationCallback;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import appeng.blockentity.spatial.SpatialAnchorBlockEntity;
import appeng.core.AppEng;

public class ChunkLoadingService implements LoadingValidationCallback {

    private static final ChunkLoadingService INSTANCE = new ChunkLoadingService();

    // Flag to ignore a server after it is stopping as grid nodes might reevaluate their grids during a shutdown.
    private boolean running = true;

    private final TicketController controller = new TicketController(AppEng.makeId("default"), this);

    public void register(RegisterTicketControllersEvent event) {
        event.register(controller);
    }

    public void onServerAboutToStart(ServerAboutToStartEvent evt) {
        this.running = true;
    }

    public void onServerStopping(ServerStoppingEvent event) {
        this.running = false;
    }

    public static ChunkLoadingService getInstance() {
        return INSTANCE;
    }

    @Override
    public void validateTickets(ServerLevel level, TicketHelper ticketHelper) {
        // Iterate over all blockpos registered as chunk loader to initialize them
        ticketHelper.getBlockTickets().forEach((blockPos, chunks) -> {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);

            // Add all persisted chunks to the list of handled ones by each anchor.
            // Or remove all in case the anchor no longer exists.
            if (blockEntity instanceof SpatialAnchorBlockEntity anchor) {
                for (Long chunk : chunks.normal()) {
                    anchor.registerChunk(new ChunkPos(chunk));
                }
                for (Long chunk : chunks.naturalSpawning()) {
                    anchor.registerChunk(new ChunkPos(chunk));
                }
            } else {
                ticketHelper.removeAllTickets(blockPos);
            }
        });
    }

    public boolean forceChunk(ServerLevel level, BlockPos owner, ChunkPos position) {
        if (running) {
            return controller.forceChunk(level, owner, position.x, position.z, true, true);
        }

        return false;
    }

    public boolean releaseChunk(ServerLevel level, BlockPos owner, ChunkPos position) {
        if (running) {
            return controller.forceChunk(level, owner, position.x, position.z, false, true);
        }

        return false;
    }

    public boolean isChunkForced(ServerLevel level, int chunkX, int chunkZ) {
        return ChunkLoadState.get(level).isForceLoaded(chunkX, chunkZ);
    }

}
