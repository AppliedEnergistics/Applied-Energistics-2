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
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * A class to hold data related to ticking block entities.
 */
class ServerBlockEntityRepo {
    record FirstTickInfo<T extends BlockEntity> (T blockEntity, Consumer<? super T> initFunction) {
        void callInit() {
            initFunction.accept(blockEntity);
        }
    }

    // Mapping is level -> encoded chunk pos -> block entities waiting to be initialized
    private final Map<LevelAccessor, Long2ObjectMap<List<FirstTickInfo<?>>>> blockEntities = new Object2ObjectOpenHashMap<>();

    /**
     * Resets all internal data
     */
    void clear() {
        this.blockEntities.clear();
    }

    /**
     * Add a new block entity to be initializes in a later tick.
     */
    synchronized <T extends BlockEntity> void addBlockEntity(T blockEntity, Consumer<? super T> initFunction) {
        final LevelAccessor level = blockEntity.getLevel();
        final int x = blockEntity.getBlockPos().getX() >> 4;
        final int z = blockEntity.getBlockPos().getZ() >> 4;
        final long chunkPos = ChunkPos.asLong(x, z);

        // Note: in some cases, the level load event might be fired after addBlockEntity is called if a mod loads chunks
        // during an earlier listener. To avoid such issues, we use computeIfAbsent in addBlockEntity directly.
        Long2ObjectMap<List<FirstTickInfo<?>>> worldQueue = this.blockEntities.computeIfAbsent(level,
                key -> new Long2ObjectOpenHashMap<>());

        worldQueue.computeIfAbsent(chunkPos, key -> new ArrayList<>())
                .add(new FirstTickInfo<>(blockEntity, initFunction));
    }

    /**
     * Tears down data related to a now unloaded level
     */
    synchronized void removeLevel(LevelAccessor level) {
        this.blockEntities.remove(level);
    }

    /**
     * Removes a unloaded chunk within a level.
     * <p>
     * There is no related addWorldChunk. The necessary queue will be created once the first block entity is added to a
     * chunk to save memory.
     */
    synchronized void removeChunk(LevelAccessor level, long chunkPos) {
        Map<Long, List<FirstTickInfo<?>>> queue = this.blockEntities.get(level);
        if (queue != null) {
            queue.remove(chunkPos);
        }
    }

    /**
     * Get the block entities needing to be initialized in this specific {@link LevelAccessor}.
     */
    public Long2ObjectMap<List<FirstTickInfo<?>>> getBlockEntities(LevelAccessor level) {
        return blockEntities.get(level);
    }

    public List<Component> getReport() {
        var result = new ArrayList<Component>();

        for (var levelEntry : blockEntities.entrySet()) {
            if (levelEntry.getValue().isEmpty()) {
                continue;
            }

            var level = levelEntry.getKey();
            String levelName = level.toString();
            if (level instanceof ServerLevel serverLevel) {
                levelName = serverLevel.dimension().location().toString();
            }

            result.add(new TextComponent(levelName).withStyle(ChatFormatting.BOLD));
            for (var chunkEntry : levelEntry.getValue().long2ObjectEntrySet()) {
                var chunkPos = new ChunkPos(chunkEntry.getLongKey());
                var line = new TextComponent(chunkPos.x + "," + chunkPos.z + ": ")
                        .withStyle(ChatFormatting.BOLD)
                        .append(Integer.toString(chunkEntry.getValue().size()));
                result.add(line);
            }
        }

        return result;
    }
}
