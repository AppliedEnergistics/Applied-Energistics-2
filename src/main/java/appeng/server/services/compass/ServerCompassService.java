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

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.jetbrains.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import appeng.blockentity.misc.MysteriousCubeBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.worldgen.meteorite.MeteoriteStructure;

public final class ServerCompassService {

    /**
     * Maximum distance to search in chunks.
     */
    private static final int MAX_RANGE = 174;
    private static final int CHUNK_SIZE = 16;

    private record Query(ServerLevel level, ChunkPos chunk) {
    }

    // We use this basic cache to prevent client-side spamming, although the client can request arbitrary
    // chunk positions and range, if malicious.
    private static final LoadingCache<Query, Optional<BlockPos>> CLOSEST_METEORITE_CACHE = CacheBuilder.newBuilder()
            .maximumSize(100)
            .weakKeys()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                @Override
                public Optional<BlockPos> load(ServerCompassService.Query query) {
                    return Optional.ofNullable(findClosestMeteoritePos(query.level, query.chunk));
                }
            });

    public static Optional<BlockPos> getClosestMeteorite(ServerLevel level, ChunkPos chunkPos) {
        return CLOSEST_METEORITE_CACHE.getUnchecked(new Query(level, chunkPos));
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
        CLOSEST_METEORITE_CACHE.invalidate(new Query(level, chunk.getPos()));
    }

    /**
     * Notifies the compass service that a skystone block has either been placed or replaced at the give position.
     */
    public static void notifyBlockChange(ServerLevel level, BlockPos pos) {
        ChunkAccess chunk = level.getChunk(pos);
        var compassRegion = CompassRegion.get(level, chunk.getPos());
        updateArea(compassRegion, chunk, level.getSectionIndex(pos.getY()));
        CLOSEST_METEORITE_CACHE.invalidate(new Query(level, chunk.getPos()));
    }

    public static void rebuild(ServerLevel level, ChunkPos center, CommandSourceStack source) {

        var meteorite = level.registryAccess()
                .lookupOrThrow(Registries.STRUCTURE)
                .getValueOrThrow(MeteoriteStructure.KEY);
        var chunkSource = level.getChunkSource();
        var futures = new ArrayList<CompletableFuture<Void>>();
        var chunkResults = new ConcurrentHashMap<ChunkPos, boolean[]>();
        for (var x = -256; x <= 256; x++) {
            for (var z = -256; z <= 256; z++) {
                var cp = new ChunkPos(center.x + x, center.z + z);
                futures.add(chunkSource.getChunkFuture(cp.x, cp.z, ChunkStatus.EMPTY, false)
                        .thenAccept(result -> {
                            var chunk = result.orElse(null);
                            if (chunk == null) {
                                return;
                            }
                            if (chunk.getHighestGeneratedStatus().isOrAfter(ChunkStatus.FEATURES)) {
                                chunkResults.put(cp, indexChunk(chunk));
                            } else if (chunk.getHighestGeneratedStatus().isOrAfter(ChunkStatus.STRUCTURE_REFERENCES)) {
                                // This causes the meteorite to generate and the chunk section to be marked
                                // automatically
                                if (!chunk.getReferencesForStructure(meteorite).isEmpty()) {
                                    chunkSource.getChunkFuture(cp.x, cp.z, ChunkStatus.FEATURES, true);
                                }
                            }
                        }));
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        source.sendSuccess(
                () -> Component.literal("Finished rebuilding compass data for " + chunkResults.size() + " chunks..."),
                true);

        var hits = 0;
        for (var entry : chunkResults.entrySet()) {
            var region = CompassRegion.get(level, entry.getKey());
            for (int i = 0; i < entry.getValue().length; i++) {
                boolean hasBlock = entry.getValue()[i];
                region.setHasCompassTarget(entry.getKey().x, entry.getKey().z, i, hasBlock);
                if (hasBlock) {
                    hits++;
                }
            }
        }

        source.sendSystemMessage(Component.literal("Found " + hits + " hits"));
        CLOSEST_METEORITE_CACHE.invalidateAll();
    }

    private static boolean[] indexChunk(ChunkAccess chunk) {
        LevelChunkSection[] chunkSections = chunk.getSections();
        boolean[] result = new boolean[chunkSections.length];
        for (var sectionIndex = 0; sectionIndex < chunkSections.length; sectionIndex++) {
            var section = chunkSections[sectionIndex];
            if (section.hasOnlyAir()) {
                result[sectionIndex] = false;
                continue;
            }

            // Count how many mysterious cubes there are
            var desiredBlock = AEBlocks.MYSTERIOUS_CUBE.block();
            var blockCount = new AtomicInteger(0);
            section.getStates().count((state, count) -> {
                if (state.is(desiredBlock)) {
                    blockCount.getAndIncrement();
                }
            });
            result[sectionIndex] = blockCount.get() > 0;
        }
        return result;
    }

    private static void updateArea(CompassRegion compassRegion, ChunkAccess chunk, int sectionIndex) {
        int cx = chunk.getPos().x;
        int cz = chunk.getPos().z;

        var section = chunk.getSections()[sectionIndex];
        if (section.hasOnlyAir()) {
            compassRegion.setHasCompassTarget(cx, cz, sectionIndex, false);
            return;
        }

        // Count how many mysterious cubes there are
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
