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

package appeng.services;

import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import appeng.api.util.DimensionalBlockPos;
import appeng.core.definitions.AEBlocks;
import appeng.services.compass.CompassReader;
import appeng.services.compass.ICompassCallback;

public final class CompassService {
    private static final int CHUNK_SIZE = 16;

    private final MinecraftServer server;
    private final WeakHashMap<ServerLevel, CompassReader> worldSet = new WeakHashMap<>(10);
    private final ExecutorService executor;

    private int jobSize;

    public CompassService(MinecraftServer server, @Nonnull final ThreadFactory factory) {
        this.server = server;
        this.executor = Executors.newSingleThreadExecutor(factory);
        this.jobSize = 0;
    }

    public Future<?> getCompassDirection(final DimensionalBlockPos coord, final int maxRange,
            final ICompassCallback cc) {
        this.jobSize++;
        return this.executor.submit(new CMDirectionRequest(coord, maxRange, cc));
    }

    /**
     * Ensure the a compass service is removed once a level gets unloaded by forge.
     *
     * @param event the event containing the unloaded level.
     */
    // FIXME this is never registered
    @SubscribeEvent
    public void unloadWorld(final WorldEvent.Unload event) {
        if (!(event.getWorld() instanceof ServerLevel level)) {
            return;
        }

        if (this.worldSet.containsKey(level)) {
            final CompassReader compassReader = this.worldSet.remove(level);
            compassReader.close();
        }
    }

    private int jobSize() {
        return this.jobSize;
    }

    private void cleanUp() {
        for (final CompassReader cr : this.worldSet.values()) {
            cr.close();
        }
    }

    public void tryUpdateArea(final ServerLevelAccessor level, ChunkPos chunkPos) {
        // If this seems weird: during worldgen, WorldAccess is a specific region, but
        // getLevel is still the server level. We do need to use the level access to get the chunk
        // in question though, since during worldgen, it's not comitted to the actual level yet.
        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);
        updateArea(level.getLevel(), chunk);
    }

    public void updateArea(final ServerLevel level, ChunkAccess chunk) {
        this.updateArea(level, chunk, CHUNK_SIZE);
        this.updateArea(level, chunk, CHUNK_SIZE + 32);
        this.updateArea(level, chunk, CHUNK_SIZE + 64);
        this.updateArea(level, chunk, CHUNK_SIZE + 96);

        this.updateArea(level, chunk, CHUNK_SIZE + 128);
        this.updateArea(level, chunk, CHUNK_SIZE + 160);
        this.updateArea(level, chunk, CHUNK_SIZE + 192);
        this.updateArea(level, chunk, CHUNK_SIZE + 224);
    }

    /**
     * Notifies the compass service that a skystone block has either been placed or replaced at the give position.
     */
    public void notifyBlockChange(final ServerLevel level, BlockPos pos) {
        ChunkAccess chunk = level.getChunk(pos);
        updateArea(level, chunk, pos.getY());
    }

    private Future<?> updateArea(final ServerLevel level, ChunkAccess c, int y) {
        this.jobSize++;

        final int cdy = y >> 5;
        final int low_y = cdy << 5;
        final int hi_y = low_y + 32;

        // lower level...
        int cx = c.getPos().x;
        int cz = c.getPos().z;

        Block skyStoneBlock = AEBlocks.SKY_STONE_BLOCK.block();
        MutableBlockPos pos = new MutableBlockPos();
        for (int i = 0; i < CHUNK_SIZE; i++) {
            pos.setX(i);
            for (int j = 0; j < CHUNK_SIZE; j++) {
                pos.setZ(j);
                for (int k = low_y; k < hi_y; k++) {
                    pos.setY(k);
                    final Block blk = c.getBlockState(pos).getBlock();
                    if (blk == skyStoneBlock) {
                        return this.executor.submit(new CMUpdatePost(level, cx, cz, cdy, true));
                    }
                }
            }
        }

        return this.executor.submit(new CMUpdatePost(level, cx, cz, cdy, false));
    }

    public void kill() {
        this.executor.shutdown();

        try {
            this.executor.awaitTermination(6, TimeUnit.MINUTES);
            this.jobSize = 0;

            for (final CompassReader cr : this.worldSet.values()) {
                cr.close();
            }

            this.worldSet.clear();
        } catch (final InterruptedException e) {
            // wrap this up..
        }
    }

    private CompassReader getReader(final ServerLevel level) {
        CompassReader cr = this.worldSet.get(level);

        if (cr == null) {
            cr = new CompassReader(level);
            this.worldSet.put(level, cr);
        }

        return cr;
    }

    private int dist(final int ax, final int az, final int bx, final int bz) {
        final int up = (bz - az) * CHUNK_SIZE;
        final int side = (bx - ax) * CHUNK_SIZE;

        return up * up + side * side;
    }

    private double rad(final int ax, final int az, final int bx, final int bz) {
        final int up = bz - az;
        final int side = bx - ax;

        return Math.atan2(-up, side) - Math.PI / 2.0;
    }

    private class CMUpdatePost implements Runnable {

        public final ServerLevel level;

        public final int chunkX;
        public final int chunkZ;
        public final int doubleChunkY; // 32 blocks instead of 16.
        public final boolean value;

        public CMUpdatePost(final ServerLevel level, final int cx, final int cz, final int dcy, final boolean val) {
            this.level = level;
            this.chunkX = cx;
            this.doubleChunkY = dcy;
            this.chunkZ = cz;
            this.value = val;
        }

        @Override
        public void run() {
            CompassService.this.jobSize--;

            final CompassReader cr = CompassService.this.getReader(this.level);
            cr.setHasBeacon(this.chunkX, this.chunkZ, this.doubleChunkY, this.value);

            if (CompassService.this.jobSize() < 2) {
                CompassService.this.cleanUp();
            }
        }
    }

    private class CMDirectionRequest implements Runnable {

        public final int maxRange;
        public final DimensionalBlockPos coord;
        public final ICompassCallback callback;

        public CMDirectionRequest(final DimensionalBlockPos coord, final int getMaxRange, final ICompassCallback cc) {
            this.coord = coord;
            this.maxRange = getMaxRange;
            this.callback = cc;
        }

        @Override
        public void run() {

            ServerLevel level = (ServerLevel) this.coord.getLevel();

            CompassService.this.jobSize--;

            final int cx = this.coord.getPos().getX() >> 4;
            final int cz = this.coord.getPos().getZ() >> 4;

            final CompassReader cr = CompassService.this.getReader(level);

            // Am I standing on it?
            if (cr.hasBeacon(cx, cz)) {
                this.callback.calculatedDirection(true, true, -999, 0);

                if (CompassService.this.jobSize() < 2) {
                    CompassService.this.cleanUp();
                }

                return;
            }

            // spiral outward...
            for (int offset = 1; offset < this.maxRange; offset++) {
                final int minX = cx - offset;
                final int minZ = cz - offset;
                final int maxX = cx + offset;
                final int maxZ = cz + offset;

                int closest = Integer.MAX_VALUE;
                int chosen_x = cx;
                int chosen_z = cz;

                for (int z = minZ; z <= maxZ; z++) {
                    if (cr.hasBeacon(minX, z)) {
                        final int closeness = CompassService.this.dist(cx, cz, minX, z);
                        if (closeness < closest) {
                            closest = closeness;
                            chosen_x = minX;
                            chosen_z = z;
                        }
                    }

                    if (cr.hasBeacon(maxX, z)) {
                        final int closeness = CompassService.this.dist(cx, cz, maxX, z);
                        if (closeness < closest) {
                            closest = closeness;
                            chosen_x = maxX;
                            chosen_z = z;
                        }
                    }
                }

                for (int x = minX + 1; x < maxX; x++) {
                    if (cr.hasBeacon(x, minZ)) {
                        final int closeness = CompassService.this.dist(cx, cz, x, minZ);
                        if (closeness < closest) {
                            closest = closeness;
                            chosen_x = x;
                            chosen_z = minZ;
                        }
                    }

                    if (cr.hasBeacon(x, maxZ)) {
                        final int closeness = CompassService.this.dist(cx, cz, x, maxZ);
                        if (closeness < closest) {
                            closest = closeness;
                            chosen_x = x;
                            chosen_z = maxZ;
                        }
                    }
                }

                if (closest < Integer.MAX_VALUE) {
                    this.callback.calculatedDirection(true, false, CompassService.this.rad(cx, cz, chosen_x, chosen_z),
                            CompassService.this.dist(cx, cz, chosen_x, chosen_z));

                    if (CompassService.this.jobSize() < 2) {
                        CompassService.this.cleanUp();
                    }

                    return;
                }
            }

            // didn't find shit...
            this.callback.calculatedDirection(false, true, -999, 999);

            if (CompassService.this.jobSize() < 2) {
                CompassService.this.cleanUp();
            }
        }
    }
}
