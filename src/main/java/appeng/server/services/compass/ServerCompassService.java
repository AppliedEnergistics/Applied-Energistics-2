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

package appeng.server.services.compass;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.event.level.LevelEvent;

import appeng.blockentity.misc.MysteriousCubeBlockEntity;
import appeng.core.definitions.AEBlocks;

public final class ServerCompassService {

    /**
     * Maximum distance to search in chunks.
     */
    private static final int MAX_RANGE = 174;
    private static final int CHUNK_SIZE = 16;

    // We use this basic cache to prevent client-side spamming, although the client can request arbitrary
    // chunk positions and range, if malicious. Caches are per level and removed when the level unloads.
    // This map is concurrent since invalidation can happen from worldgen threads.
    private static final Map<ServerLevel, LoadingCache<ChunkPos, Optional<BlockPos>>> CLOSEST_METEORITE_CACHE = new ConcurrentHashMap<>();

    private static LoadingCache<ChunkPos, Optional<BlockPos>> createCache(ServerLevel level) {
        return CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .build(CacheLoader.from(chunkPos -> Optional.ofNullable(findClosestMeteoritePos(level, chunkPos))));
    }

    public static Optional<BlockPos> getClosestMeteorite(ServerLevel level, ChunkPos chunkPos) {
        return CLOSEST_METEORITE_CACHE.computeIfAbsent(level, ServerCompassService::createCache)
                .getUnchecked(chunkPos);
    }

    /**
     * A change in one chunk can affect the result of searches originating in any chunk within range, so we invalidate
     * all cached searches for the level.
     */
    private static void invalidateCache(ServerLevel level) {
        // Do not create a cache here, since this may be called from worldgen threads
        var cache = CLOSEST_METEORITE_CACHE.get(level);
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            CLOSEST_METEORITE_CACHE.remove(level);
        }
    }

    @Nullable
    private static BlockPos findClosestMeteoritePos(ServerLevel level, ChunkPos originChunkPos) {
        var chunkPos = findClosestMeteoriteChunk(level, originChunkPos);
        if (chunkPos == null) {
            return null;
        }
        var chunk = level.getChunkSource().getChunk(chunkPos.x, chunkPos.z, false);
        if (chunk == null) {
            // Do not chunk-load a chunk to check for a precise block position
            return chunkPos.getMiddleBlockPosition(0);
        }

        // Find the closest BE in the chunk. Usually it will only be one.
        var sourcePos = originChunkPos.getMiddleBlockPosition(0);
        var closestDistanceSq = Double.MAX_VALUE;
        BlockPos chosenPos = chunkPos.getMiddleBlockPosition(0);
        for (var blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof MysteriousCubeBlockEntity) {
                var bePos = blockEntity.getBlockPos().atY(0);
                var distSq = sourcePos.distSqr(bePos);
                if (distSq < closestDistanceSq) {
                    chosenPos = bePos;
                    closestDistanceSq = distSq;
                }
            }
        }
        return chosenPos;
    }

    @Nullable
    private static ChunkPos findClosestMeteoriteChunk(ServerLevel level, ChunkPos chunkPos) {
        var cx = chunkPos.x;
        var cz = chunkPos.z;

        // Am I standing on it?
        if (CompassRegion.hasCompassTarget(level, cx, cz)) {
            return chunkPos;
        }

        // spiral outward...
        for (int offset = 1; offset < MAX_RANGE; offset++) {
            final int minX = cx - offset;
            final int minZ = cz - offset;
            final int maxX = cx + offset;
            final int maxZ = cz + offset;

            int closest = Integer.MAX_VALUE;
            int chosen_x = cx;
            int chosen_z = cz;

            for (int z = minZ; z <= maxZ; z++) {
                if (CompassRegion.hasCompassTarget(level, minX, z)) {
                    final int closeness = dist(cx, cz, minX, z);
                    if (closeness < closest) {
                        closest = closeness;
                        chosen_x = minX;
                        chosen_z = z;
                    }
                }

                if (CompassRegion.hasCompassTarget(level, maxX, z)) {
                    final int closeness = dist(cx, cz, maxX, z);
                    if (closeness < closest) {
                        closest = closeness;
                        chosen_x = maxX;
                        chosen_z = z;
                    }
                }
            }

            for (int x = minX + 1; x < maxX; x++) {
                if (CompassRegion.hasCompassTarget(level, x, minZ)) {
                    final int closeness = dist(cx, cz, x, minZ);
                    if (closeness < closest) {
                        closest = closeness;
                        chosen_x = x;
                        chosen_z = minZ;
                    }
                }

                if (CompassRegion.hasCompassTarget(level, x, maxZ)) {
                    final int closeness = dist(cx, cz, x, maxZ);
                    if (closeness < closest) {
                        closest = closeness;
                        chosen_x = x;
                        chosen_z = maxZ;
                    }
                }
            }

            if (closest < Integer.MAX_VALUE) {
                return new ChunkPos(chosen_x, chosen_z);
            }
        }

        // didn't find shit...
        return null;
    }

    public static void updateArea(ServerLevel level, ChunkAccess chunk) {
        var compassRegion = CompassRegion.get(level, chunk.getPos());

        for (var i = 0; i < level.getSectionsCount(); i++) {
            updateArea(compassRegion, chunk, i);
        }
        invalidateCache(level);
    }

    /**
     * Notifies the compass service that a skystone block has either been placed or replaced at the give position.
     */
    public static void notifyBlockChange(ServerLevel level, BlockPos pos) {
        ChunkAccess chunk = level.getChunk(pos);
        var compassRegion = CompassRegion.get(level, chunk.getPos());
        updateArea(compassRegion, chunk, level.getSectionIndex(pos.getY()));
        invalidateCache(level);
    }

    private static void updateArea(CompassRegion compassRegion, ChunkAccess chunk, int sectionIndex) {
        int cx = chunk.getPos().x;
        int cz = chunk.getPos().z;

        var section = chunk.getSections()[sectionIndex];
        if (section.hasOnlyAir()) {
            compassRegion.setHasCompassTarget(cx, cz, sectionIndex, false);
            return;
        }

        // Count how many skystone blocks there are
        var desiredState = AEBlocks.MYSTERIOUS_CUBE.block().defaultBlockState();
        var blockCount = new AtomicInteger(0);
        section.getStates().count((state, count) -> {
            if (state == desiredState) {
                blockCount.getAndIncrement();
            }
        });
        compassRegion.setHasCompassTarget(cx, cz, sectionIndex, blockCount.get() > 0);
    }

    private static int dist(int ax, int az, int bx, int bz) {
        final int up = (bz - az) * CHUNK_SIZE;
        final int side = (bx - ax) * CHUNK_SIZE;

        return up * up + side * side;
    }

}
