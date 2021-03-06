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

package appeng.spatial;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;

import appeng.api.movable.IMovableHandler;
import appeng.api.movable.IMovableRegistry;
import appeng.api.util.AEPartLocation;
import appeng.api.util.WorldCoord;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.worlddata.WorldData;

public class CachedPlane {
    private final int x_size;
    private final int z_size;
    private final int cx_size;
    private final int cz_size;
    private final int x_offset;
    private final int y_offset;
    private final int z_offset;
    private final int y_size;
    private final WorldChunk[][] myChunks;
    private final Column[][] myColumns;
    private final List<BlockEntity> tiles = new ArrayList<>();
    private final List<ScheduledTick<Block>> ticks = new ArrayList<>();
    private final ServerWorld world;
    private final IMovableRegistry reg = Api.instance().registries().movable();
    private final List<WorldCoord> updates = new ArrayList<>();
    private int verticalBits;
    private final BlockState matrixBlockState;

    public CachedPlane(final ServerWorld w, final int minX, final int minY, final int minZ, final int maxX,
            final int maxY, final int maxZ) {

        Block matrixFrameBlock = Api.instance().definitions().blocks().matrixFrame().maybeBlock().orElse(null);
        if (matrixFrameBlock != null) {
            this.matrixBlockState = matrixFrameBlock.getDefaultState();
        } else {
            this.matrixBlockState = null;
        }

        this.world = w;

        this.x_size = maxX - minX + 1;
        this.y_size = maxY - minY + 1;
        this.z_size = maxZ - minZ + 1;

        this.x_offset = minX;
        this.y_offset = minY;
        this.z_offset = minZ;

        final int minCX = minX >> 4;
        final int minCY = minY >> 4;
        final int minCZ = minZ >> 4;
        final int maxCX = maxX >> 4;
        final int maxCY = maxY >> 4;
        final int maxCZ = maxZ >> 4;

        this.cx_size = maxCX - minCX + 1;
        final int cy_size = maxCY - minCY + 1;
        this.cz_size = maxCZ - minCZ + 1;

        this.myChunks = new WorldChunk[this.cx_size][this.cz_size];
        this.myColumns = new Column[this.x_size][this.z_size];

        this.verticalBits = 0;
        for (int cy = 0; cy < cy_size; cy++) {
            this.verticalBits |= 1 << (minCY + cy);
        }

        for (int x = 0; x < this.x_size; x++) {
            for (int z = 0; z < this.z_size; z++) {
                this.myColumns[x][z] = new Column(w.getChunk((minX + x) >> 4, (minZ + z) >> 4), (minX + x) & 0xF,
                        (minZ + z) & 0xF, minCY, cy_size);
            }
        }

        final IMovableRegistry mr = Api.instance().registries().movable();

        for (int cx = 0; cx < this.cx_size; cx++) {
            for (int cz = 0; cz < this.cz_size; cz++) {
                final List<BlockPos> deadTiles = new ArrayList<>();

                final WorldChunk c = w.getChunk(minCX + cx, minCZ + cz);
                this.myChunks[cx][cz] = c;

                Set<BlockPos> blockEntityPositions = c.getBlockEntityPositions();
                for (BlockPos tePOS : blockEntityPositions) {
                    final BlockEntity te = c.getBlockEntity(tePOS);
                    if (te == null) {
                        continue;
                    }

                    if (tePOS.getX() >= minX && tePOS.getX() <= maxX && tePOS.getY() >= minY && tePOS.getY() <= maxY
                            && tePOS.getZ() >= minZ && tePOS.getZ() <= maxZ) {
                        if (mr.askToMove(te)) {
                            this.tiles.add(te);
                            deadTiles.add(tePOS);
                        } else {
                            final BlockStorageData details = new BlockStorageData();
                            this.myColumns[tePOS.getX() - minX][tePOS.getZ() - minZ].fillData(tePOS.getY(), details);

                            // don't skip air, just let the code replace it...
                            if (details.state.isAir()) {
                                w.removeBlock(tePOS, false);
                            } else {
                                this.myColumns[tePOS.getX() - minX][tePOS.getZ() - minZ].setSkip(tePOS.getY());
                            }
                        }
                    }
                }

                for (final BlockPos cp : deadTiles) {
                    c.removeBlockEntity(cp);
                }

                final long gameTime = this.getWorld().getTime();
                final TickScheduler<Block> pendingBlockTicks = this.getWorld().getBlockTickScheduler();
                if (pendingBlockTicks instanceof ServerTickScheduler) {
                    List<ScheduledTick<Block>> pending = ((ServerTickScheduler<Block>) pendingBlockTicks)
                            .getScheduledTicksInChunk(c.getPos(), false, true);
                    for (final ScheduledTick<Block> entry : pending) {
                        final BlockPos tePOS = entry.pos;
                        if (tePOS.getX() >= minX && tePOS.getX() <= maxX && tePOS.getY() >= minY && tePOS.getY() <= maxY
                                && tePOS.getZ() >= minZ && tePOS.getZ() <= maxZ) {
                            this.ticks.add(new ScheduledTick<>(tePOS, entry.getObject(), entry.time - gameTime,
                                    entry.priority));
                        }
                    }
                }
            }
        }

        for (final BlockEntity te : this.tiles) {
            try {
                /*
                 * FIXME 1.17 this.getWorld().blockEntities.remove(te); if (te instanceof BlockEntityTicker) {
                 * this.getWorld().tickingBlockEntities.remove(te); }
                 */
            } catch (final Exception e) {
                AELog.debug(e);
            }
        }
    }

    private IMovableHandler getHandler(final BlockEntity te) {
        final IMovableRegistry mr = Api.instance().registries().movable();
        return mr.getHandler(te);
    }

    void swap(final CachedPlane dst) {
        final IMovableRegistry mr = Api.instance().registries().movable();

        if (dst.x_size == this.x_size && dst.y_size == this.y_size && dst.z_size == this.z_size) {
            AELog.info("Block Copy Scale: " + this.x_size + ", " + this.y_size + ", " + this.z_size);

            long startTime = System.nanoTime();
            final BlockStorageData aD = new BlockStorageData();
            final BlockStorageData bD = new BlockStorageData();

            for (int x = 0; x < this.x_size; x++) {
                for (int z = 0; z < this.z_size; z++) {
                    final Column a = this.myColumns[x][z];
                    final Column b = dst.myColumns[x][z];

                    for (int y = 0; y < this.y_size; y++) {
                        final int src_y = y + this.y_offset;
                        final int dst_y = y + dst.y_offset;

                        if (a.doNotSkip(src_y) && b.doNotSkip(dst_y)) {
                            a.fillData(src_y, aD);
                            b.fillData(dst_y, bD);

                            a.setBlockState(src_y, bD);
                            b.setBlockState(dst_y, aD);
                        } else {
                            this.markForUpdate(x + this.x_offset, src_y, z + this.z_offset);
                            dst.markForUpdate(x + dst.x_offset, dst_y, z + dst.z_offset);
                        }
                    }
                }
            }

            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            AELog.info("Block Copy Time: " + duration);

            for (final BlockEntity te : this.tiles) {
                final BlockPos tePOS = te.getPos();
                dst.addTile(tePOS.getX() - this.x_offset, tePOS.getY() - this.y_offset, tePOS.getZ() - this.z_offset,
                        te, this, mr);
            }

            for (final BlockEntity te : dst.tiles) {
                final BlockPos tePOS = te.getPos();
                this.addTile(tePOS.getX() - dst.x_offset, tePOS.getY() - dst.y_offset, tePOS.getZ() - dst.z_offset, te,
                        dst, mr);
            }

            for (final ScheduledTick<Block> entry : this.ticks) {
                final BlockPos tePOS = entry.pos;
                dst.addTick(tePOS.getX() - this.x_offset, tePOS.getY() - this.y_offset, tePOS.getZ() - this.z_offset,
                        entry);
            }

            for (final ScheduledTick<Block> entry : dst.ticks) {
                final BlockPos tePOS = entry.pos;
                this.addTick(tePOS.getX() - dst.x_offset, tePOS.getY() - dst.y_offset, tePOS.getZ() - dst.z_offset,
                        entry);
            }

            startTime = System.nanoTime();
            this.updateChunks();
            dst.updateChunks();
            endTime = System.nanoTime();

            duration = endTime - startTime;
            AELog.info("Update Time: " + duration);
        }
    }

    private void markForUpdate(final int x, final int y, final int z) {
        this.updates.add(new WorldCoord(x, y, z));
        for (final AEPartLocation d : AEPartLocation.SIDE_LOCATIONS) {
            this.updates.add(new WorldCoord(x + d.xOffset, y + d.yOffset, z + d.zOffset));
        }
    }

    private void addTick(final int x, final int y, final int z, final ScheduledTick<Block> entry) {
        BlockPos where = new BlockPos(x + this.x_offset, y + this.y_offset, z + this.z_offset);
        this.world.getBlockTickScheduler().schedule(where, entry.getObject(), (int) entry.time, entry.priority);
    }

    private void addTile(final int x, final int y, final int z, final BlockEntity te,
            final CachedPlane alternateDestination, final IMovableRegistry mr) {
        try {
            final Column c = this.myColumns[x][z];

            if (c.doNotSkip(y + this.y_offset) || alternateDestination == null) {
                final IMovableHandler handler = this.getHandler(te);

                try {
                    handler.moveTile(te, this.world,
                            new BlockPos(x + this.x_offset, y + this.y_offset, z + this.z_offset));
                } catch (final Throwable e) {
                    AELog.debug(e);

                    final BlockPos pos = new BlockPos(x, y, z);

                    // attempt recovery...
                    // FIXME 1.17 c.c.setBlockEntity(pos, te);

                    this.world.updateListeners(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), z);
                }

                mr.doneMoving(te);
            } else {
                alternateDestination.addTile(x, y, z, te, null, mr);
            }
        } catch (final Throwable e) {
            AELog.debug(e);
        }
    }

    private void updateChunks() {

        LightingProvider lightManager = world.getLightingProvider();

        // update shit..
        if (lightManager instanceof ServerLightingProvider) {
            ServerLightingProvider serverLightManager = (ServerLightingProvider) lightManager;
            for (int x = 0; x < this.cx_size; x++) {
                for (int z = 0; z < this.cz_size; z++) {
                    final WorldChunk c = this.myChunks[x][z];
                    serverLightManager.light(c, false);
                    c.markDirty();
                }
            }
        }

        // send shit...
        for (int x = 0; x < this.cx_size; x++) {
            for (int z = 0; z < this.cz_size; z++) {

                final WorldChunk c = this.myChunks[x][z];

                WorldData.instance().compassData().service().updateArea((ServerWorld) this.getWorld(), c);

                // FIXME this was sending chunks to players...
                ChunkDataS2CPacket cdp = new ChunkDataS2CPacket(c);
                world.getChunkManager().threadedAnvilChunkStorage.getPlayersWatchingChunk(c.getPos(), false)
                        .forEach(spe -> spe.networkHandler.sendPacket(cdp));

            }
        }

        // FIXME check if this makes any sense at all to send changes to players asap
        ServerChunkManager serverChunkProvider = world.getChunkManager();
        serverChunkProvider.tick(() -> false);
    }

    List<WorldCoord> getUpdates() {
        return this.updates;
    }

    World getWorld() {
        return this.world;
    }

    private static class BlockStorageData {
        public BlockState state;
    }

    private class Column {
        private final int x;
        private final int z;
        private final Chunk c;
        private List<Integer> skipThese = null;

        public Column(final Chunk chunk, final int x, final int z, final int chunkY, final int chunkHeight) {
            this.x = x;
            this.z = z;
            this.c = chunk;

            final ChunkSection[] storage = this.c.getSectionArray();

            // make sure storage exists before hand...
            for (int ay = 0; ay < chunkHeight; ay++) {
                final int by = (ay + chunkY);
                ChunkSection extendedblockstorage = storage[by];
                if (extendedblockstorage == null) {
                    extendedblockstorage = storage[by] = new ChunkSection(by << 4);
                }
            }
        }

        private void setBlockState(final int y, BlockStorageData data) {
            if (data.state == CachedPlane.this.matrixBlockState) {
                data.state = Blocks.AIR.getDefaultState();
            }
            final ChunkSection[] storage = this.c.getSectionArray();
            final ChunkSection extendedBlockStorage = storage[y >> 4];
            extendedBlockStorage.setBlockState(this.x, y & 15, this.z, data.state);
        }

        private void fillData(final int y, BlockStorageData data) {
            final ChunkSection[] storage = this.c.getSectionArray();
            final ChunkSection extendedblockstorage = storage[y >> 4];

            data.state = extendedblockstorage.getBlockState(this.x, y & 15, this.z);
        }

        private boolean doNotSkip(final int y) {
            final ChunkSection[] storage = this.c.getSectionArray();
            final ChunkSection extendedblockstorage = storage[y >> 4];
            if (CachedPlane.this.reg
                    .isBlacklisted(extendedblockstorage.getBlockState(this.x, y & 15, this.z).getBlock())) {
                return false;
            }

            return this.skipThese == null || !this.skipThese.contains(y);
        }

        private void setSkip(final int yCoord) {
            if (this.skipThese == null) {
                this.skipThese = new ArrayList<>();
            }
            this.skipThese.add(yCoord);
        }
    }
}
