/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.spatial;

import java.util.List;

import javax.annotation.Nullable;

import appeng.core.AppEng;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import appeng.core.AELog;
import appeng.core.definitions.AEBlocks;

/**
 * Allocates and manages plots for spatial storage in the spatial storage world.
 */
public final class SpatialStoragePlotManager {

    public static final SpatialStoragePlotManager INSTANCE = new SpatialStoragePlotManager();

    private SpatialStoragePlotManager() {
    }

    /**
     * Gets the world used to store spatial storage cell's content.
     */
    public ServerLevel getWorld() {
        var server = AppEng.instance().getCurrentServer();
        if (server == null) {
            throw new IllegalStateException("No server is currently running.");
        }
        ServerLevel world = server.getLevel(SpatialStorageDimensionIds.WORLD_ID);
        if (world == null) {
            throw new IllegalStateException("The storage cell world is missing.");
        }
        return world;
    }

    private SpatialStorageWorldData getWorldData() {
        return getWorld().getChunkSource().getDataStorage().computeIfAbsent(
                SpatialStorageWorldData::load,
                SpatialStorageWorldData::new,
                SpatialStorageWorldData.ID
        );
    }

    @Nullable
    public SpatialStoragePlot getPlot(int plotId) {
        if (plotId == -1) {
            return null;
        }
        return getWorldData().getPlotById(plotId);
    }

    public SpatialStoragePlot allocatePlot(BlockPos size, int ownerId) {
        SpatialStoragePlot plot = getWorldData().allocatePlot(size, ownerId);
        AELog.info("Allocating storage cell plot %d with size %s for %d", plot.getId(), size, ownerId);
        return plot;
    }

    /**
     * Sets the last source for a spatial storage transition into the given plot. This is used to allow the server-admin
     * commands to know where the content of a given plot came from.
     */
    public void setLastTransition(int plotId, TransitionInfo info) {
        getWorldData().setLastTransition(plotId, info);
    }

    /**
     * Returns an immutable list of all plots.
     */
    public List<SpatialStoragePlot> getPlots() {
        return getWorldData().getPlots();
    }

    public void freePlot(int plotId, boolean resetBlocks) {
        SpatialStoragePlot plot = getPlot(plotId);
        if (plot == null) {
            return; // Already removed apparently
        }

        if (resetBlocks) {
            BlockPos from = plot.getOrigin();
            BlockPos to = from.offset(plot.getSize()).offset(-1, -1, -1);

            AELog.info("Clearing spatial storage plot %s (%s -> %s)", plotId, from, to);

            // This is slow, but it should usually be just an admin-command
            ServerLevel world = getWorld();
            BlockState matrixFrame = AEBlocks.MATRIX_FRAME.block().defaultBlockState();
            for (BlockPos blockPos : BlockPos.betweenClosed(from, to)) {
                world.setBlockAndUpdate(blockPos, matrixFrame);
            }
        }

        getWorldData().removePlot(plotId);
    }

}
