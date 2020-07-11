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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import appeng.api.util.DimensionalCoord;
import appeng.core.Api;
import appeng.services.compass.CompassReader;
import appeng.services.compass.ICompassCallback;
import appeng.util.Platform;

public final class CompassService {
    private static final int CHUNK_SIZE = 16;

    private final MinecraftServer server;
    private final Map<DimensionType, CompassReader> worldSet = new HashMap<>(10);
    private final ExecutorService executor;

    private int jobSize;

    public CompassService(MinecraftServer server, @Nonnull final ThreadFactory factory) {
        this.server = server;
        this.executor = Executors.newSingleThreadExecutor(factory);
        this.jobSize = 0;
    }

    public Future<?> getCompassDirection(final DimensionalCoord coord, final int maxRange, final ICompassCallback cc) {
        this.jobSize++;
        return this.executor.submit(new CMDirectionRequest(coord, maxRange, cc));
    }

    /**
     * Ensure the a compass service is removed once a world gets unloaded by forge.
     *
     * @param event the event containing the unloaded world.
     */
    // FIXME this is never registered
    @SubscribeEvent
    public void unloadWorld(final WorldEvent.Unload event) {
        DimensionType dim = event.getWorld().getDimension().getType();

        if (Platform.isServer() && this.worldSet.containsKey(dim)) {
            final CompassReader compassReader = this.worldSet.remove(dim);
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

    public void updateArea(final IWorld w, ChunkPos chunkPos) {
        this.updateArea(w, chunkPos, CHUNK_SIZE);
        this.updateArea(w, chunkPos, CHUNK_SIZE + 32);
        this.updateArea(w, chunkPos, CHUNK_SIZE + 64);
        this.updateArea(w, chunkPos, CHUNK_SIZE + 96);

        this.updateArea(w, chunkPos, CHUNK_SIZE + 128);
        this.updateArea(w, chunkPos, CHUNK_SIZE + 160);
        this.updateArea(w, chunkPos, CHUNK_SIZE + 192);
        this.updateArea(w, chunkPos, CHUNK_SIZE + 224);
    }

    public Future<?> updateArea(final IWorld w, ChunkPos chunkPos, int y) {
        this.jobSize++;

        final int cx = chunkPos.x;
        final int cdy = y >> 5;
        final int cz = chunkPos.z;

        final int low_y = cdy << 5;
        final int hi_y = low_y + 32;

        // lower level...
        final IChunk c = w.getChunk(cx, cz);

        DimensionType dim = w.getDimension().getType();

        Block skyStoneBlock = Api.instance().definitions().blocks().skyStoneBlock().block();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int i = 0; i < CHUNK_SIZE; i++) {
            pos.setX(i);
            for (int j = 0; j < CHUNK_SIZE; j++) {
                pos.setZ(j);
                for (int k = low_y; k < hi_y; k++) {
                    pos.setY(k);
                    final Block blk = c.getBlockState(pos).getBlock();
                    if (blk == skyStoneBlock) {
                        return this.executor.submit(new CMUpdatePost(dim, cx, cz, cdy, true));
                    }
                }
            }
        }

        return this.executor.submit(new CMUpdatePost(dim, cx, cz, cdy, false));
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

    private CompassReader getReader(final DimensionType dim) {
        CompassReader cr = this.worldSet.get(dim);

        if (cr == null) {
            ServerWorld sw = server.getWorld(dim);
            cr = new CompassReader(sw);
            this.worldSet.put(dim, cr);
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

        public final DimensionType dim;

        public final int chunkX;
        public final int chunkZ;
        public final int doubleChunkY; // 32 blocks instead of 16.
        public final boolean value;

        public CMUpdatePost(final DimensionType dim, final int cx, final int cz, final int dcy, final boolean val) {
            this.dim = dim;
            this.chunkX = cx;
            this.doubleChunkY = dcy;
            this.chunkZ = cz;
            this.value = val;
        }

        @Override
        public void run() {
            CompassService.this.jobSize--;

            final CompassReader cr = CompassService.this.getReader(this.dim);
            cr.setHasBeacon(this.chunkX, this.chunkZ, this.doubleChunkY, this.value);

            if (CompassService.this.jobSize() < 2) {
                CompassService.this.cleanUp();
            }
        }
    }

    private class CMDirectionRequest implements Runnable {

        public final int maxRange;
        public final DimensionalCoord coord;
        public final ICompassCallback callback;

        public CMDirectionRequest(final DimensionalCoord coord, final int getMaxRange, final ICompassCallback cc) {
            this.coord = coord;
            this.maxRange = getMaxRange;
            this.callback = cc;
        }

        @Override
        public void run() {
            CompassService.this.jobSize--;

            final int cx = this.coord.x >> 4;
            final int cz = this.coord.z >> 4;

            final CompassReader cr = CompassService.this.getReader(this.coord.getWorld().getDimension().getType());

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
