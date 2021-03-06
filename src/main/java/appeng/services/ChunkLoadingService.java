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

package appeng.services;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.common.world.ForgeChunkManager.LoadingValidationCallback;
import net.minecraftforge.common.world.ForgeChunkManager.TicketHelper;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

import appeng.core.AppEng;
import appeng.tile.spatial.SpatialAnchorTileEntity;

public class ChunkLoadingService implements LoadingValidationCallback {

    private static final ChunkLoadingService INSTANCE = new ChunkLoadingService();

    // Flag to ignore a server after it is stopping as grid nodes might reevaluate their grids during a shutdown.
    private boolean running = true;

    public static void register() {
        ForgeChunkManager.setForcedChunkLoadingCallback(AppEng.MOD_ID, INSTANCE);
    }

    public void onServerAboutToStart(FMLServerAboutToStartEvent evt) {
        this.running = true;
    }

    public void onServerStopping(FMLServerStoppingEvent event) {
        this.running = false;
    }

    public static ChunkLoadingService getInstance() {
        return INSTANCE;
    }

    @Override
    public void validateTickets(ServerWorld world, TicketHelper ticketHelper) {
        // Iterate over all blockpos registered as chunk loader to initialize them
        ticketHelper.getBlockTickets().forEach((blockPos, chunks) -> {
            TileEntity tileEntity = world.getTileEntity(blockPos);

            // Add all persisted chunks to the list of handled ones by each anchor.
            // Or remove all in case the anchor no longer exists.
            if (tileEntity instanceof SpatialAnchorTileEntity) {
                SpatialAnchorTileEntity anchor = (SpatialAnchorTileEntity) tileEntity;
                for (Long chunk : chunks.getSecond()) {
                    anchor.registerChunk(new ChunkPos(chunk.longValue()));
                }
            } else {
                ticketHelper.removeAllTickets(blockPos);
            }
        });
    }

    public boolean forceChunk(ServerWorld world, BlockPos owner, ChunkPos position, boolean ticking) {
        if (running) {
            return ForgeChunkManager.forceChunk(world, AppEng.MOD_ID, owner, position.x, position.z, true, true);
        }

        return false;
    }

    public boolean releaseChunk(ServerWorld world, BlockPos owner, ChunkPos position, boolean ticking) {
        if (running) {
            return ForgeChunkManager.forceChunk(world, AppEng.MOD_ID, owner, position.x, position.z, false, true);
        }

        return false;
    }

}
