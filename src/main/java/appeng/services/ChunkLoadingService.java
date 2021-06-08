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

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;

public class ChunkLoadingService /* implements LoadingValidationCallback */ {

    private static final ChunkLoadingService INSTANCE = new ChunkLoadingService();

    // Flag to ignore a server after it is stopping as grid nodes might reevaluate their grids during a shutdown.
    private boolean running = true;

    public static void register() {
        // FIXME FABRIC ForgeChunkManager.setForcedChunkLoadingCallback(AppEng.MOD_ID, INSTANCE);
    }

    // FIXME FABRIC public void onServerAboutToStart(FMLServerAboutToStartEvent evt) {
    // FIXME FABRIC this.running = true;
    // FIXME FABRIC }

    // FIXME FABRIC public void onServerStopping(FMLServerStoppingEvent event) {
    // FIXME FABRIC this.running = false;
    // FIXME FABRIC }

    public static ChunkLoadingService getInstance() {
        return INSTANCE;
    }

// FIXME FABRIC     @Override
// FIXME FABRIC     public void validateTickets(ServerWorld world, TicketHelper ticketHelper) {
// FIXME FABRIC         // Iterate over all blockpos registered as chunk loader to initialize them
// FIXME FABRIC         ticketHelper.getBlockTickets().forEach((blockPos, chunks) -> {
// FIXME FABRIC             TileEntity tileEntity = world.getTileEntity(blockPos);
// FIXME FABRIC
// FIXME FABRIC             // Add all persisted chunks to the list of handled ones by each anchor.
// FIXME FABRIC             // Or remove all in case the anchor no longer exists.
// FIXME FABRIC             if (tileEntity instanceof SpatialAnchorTileEntity) {
// FIXME FABRIC                 SpatialAnchorTileEntity anchor = (SpatialAnchorTileEntity) tileEntity;
// FIXME FABRIC                 for (Long chunk : chunks.getSecond()) {
// FIXME FABRIC                     anchor.registerChunk(new ChunkPos(chunk.longValue()));
// FIXME FABRIC                 }
// FIXME FABRIC             } else {
// FIXME FABRIC                 ticketHelper.removeAllTickets(blockPos);
// FIXME FABRIC             }
// FIXME FABRIC         });
// FIXME FABRIC     }

    public boolean forceChunk(ServerWorld world, BlockPos owner, ChunkPos position, boolean ticking) {
        if (running) {
            // FIXME FABRIC return ForgeChunkManager.forceChunk(world, AppEng.MOD_ID, owner, position.x, position.z,
            // true, true);
        }

        return false;
    }

    public boolean releaseChunk(ServerWorld world, BlockPos owner, ChunkPos position, boolean ticking) {
        if (running) {
            // FIXME FABRIC return ForgeChunkManager.forceChunk(world, AppEng.MOD_ID, owner, position.x, position.z,
            // false, true);
        }

        return false;
    }

}
