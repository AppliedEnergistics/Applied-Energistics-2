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

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import appeng.blockentity.AEBaseBlockEntity;

/**
 * A class to hold data related to ticking block entities.
 */
class ServerBlockEntityRepo {

    // Mapping is world -> encoded chunk pos -> block entities waiting to be initialized
    private final Map<LevelAccessor, Long2ObjectMap<List<AEBaseBlockEntity>>> blockEntities = new Object2ObjectOpenHashMap<>();

    /**
     * Resets all internal data
     */
    void clear() {
        this.blockEntities.clear();
    }

    /**
     * Add a new block entity to be initializes in a later tick.
     */
    synchronized void addBlockEntity(AEBaseBlockEntity blockEntity) {
        final LevelAccessor world = blockEntity.getLevel();
        final int x = blockEntity.getBlockPos().getX() >> 4;
        final int z = blockEntity.getBlockPos().getZ() >> 4;
        final long chunkPos = ChunkPos.asLong(x, z);

        Long2ObjectMap<List<AEBaseBlockEntity>> worldQueue = this.blockEntities.get(world);

        worldQueue.computeIfAbsent(chunkPos, key -> new ArrayList<>()).add(blockEntity);
    }

    /**
     * Sets up the necessary defaults when a new world is loaded
     */
    synchronized void addWorld(LevelAccessor world) {
        this.blockEntities.computeIfAbsent(world, key -> new Long2ObjectOpenHashMap<>());
    }

    /**
     * Tears down data related to a now unloaded world
     */
    synchronized void removeWorld(LevelAccessor world) {
        this.blockEntities.remove(world);
    }

    /**
     * Removes a unloaded chunk within a world.
     * <p>
     * There is no related addWorldChunk. The necessary queue will be created once the first block entity is added to a chunk to
     * save memory.
     */
    synchronized void removeWorldChunk(LevelAccessor world, long chunkPos) {
        Map<Long, List<AEBaseBlockEntity>> queue = this.blockEntities.get(world);
        if (queue != null) {
            queue.remove(chunkPos);
        }
    }

    /**
     * Get the block entities needing to be initialized in this specific {@link LevelAccessor}.
     */
    public Long2ObjectMap<List<AEBaseBlockEntity>> getBlockEntities(LevelAccessor world) {
        return blockEntities.get(world);
    }

}
