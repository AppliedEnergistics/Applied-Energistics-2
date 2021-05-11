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

package appeng.hooks.ticking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import appeng.tile.AEBaseTileEntity;

/**
 * A class to hold data related to ticking tiles.
 */
class ServerTileRepo {

    // Mapping is world -> encoded chunk pos -> tile entities waiting to be initialized
    private final Map<IWorld, Long2ObjectMap<List<AEBaseTileEntity>>> tiles = new Object2ObjectOpenHashMap<>();

    /**
     * Resets all internal data
     */
    void clear() {
        this.tiles.clear();
    }

    /**
     * Add a new tile to be initializes in a later tick.
     */
    synchronized void addTile(AEBaseTileEntity tile) {
        final IWorld world = tile.getWorld();
        final int x = tile.getPos().getX() >> 4;
        final int z = tile.getPos().getZ() >> 4;
        final long chunkPos = ChunkPos.asLong(x, z);

        Long2ObjectMap<List<AEBaseTileEntity>> worldQueue = this.tiles.get(world);

        worldQueue.computeIfAbsent(chunkPos, key -> {
            return new ArrayList<>();
        }).add(tile);
    }

    /**
     * Sets up the necessary defaults when a new world is loaded
     */
    synchronized void addWorld(IWorld world) {
        this.tiles.computeIfAbsent(world, (key) -> {
            return new Long2ObjectOpenHashMap<>();
        });
    }

    /**
     * Tears down data related to a now unloaded world
     */
    synchronized void removeWorld(IWorld world) {
        this.tiles.remove(world);
    }

    /**
     * Removes a unloaded chunk within a world.
     * <p>
     * There is no related addWorldChunk. The necessary queue will be created once the first tile is added to a chunk to
     * save memory.
     */
    synchronized void removeWorldChunk(IWorld world, long chunkPos) {
        Map<Long, List<AEBaseTileEntity>> queue = this.tiles.get(world);
        if (queue != null) {
            queue.remove(chunkPos);
        }
    }

    /**
     * Get the tiles needing to be initialized in this specific {@link IWorld}.
     */
    public Long2ObjectMap<List<AEBaseTileEntity>> getTiles(IWorld world) {
        return tiles.get(world);
    }

}
