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
import java.util.Map.Entry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LevelLightEngine;

import appeng.api.movable.IMovableHandler;
import appeng.api.movable.IMovableRegistry;
import appeng.api.util.AEPartLocation;
import appeng.api.util.WorldCoord;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.definitions.AEBlocks;
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
    private final LevelChunk[][] myChunks;
    private final Column[][] myColumns;
    private final List<BlockEntity> blockEntities = new ArrayList<>();
    private final List<TickNextTickData<Block>> ticks = new ArrayList<>();
    private final ServerLevel level;
    private final IMovableRegistry reg = Api.instance().registries().movable();
    private final List<WorldCoord> updates = new ArrayList<>();
    private final BlockState matrixBlockState;

    public CachedPlane(final ServerLevel w, final int minX, final int minY, final int minZ, final int maxX,
            final int maxY, final int maxZ) {

        Block matrixFrameBlock = AEBlocks.MATRIX_FRAME.block();
        if (matrixFrameBlock != null) {
            this.matrixBlockState = matrixFrameBlock.defaultBlockState();
        } else {
            this.matrixBlockState = null;
        }

        this.level = w;

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

        this.myChunks = new LevelChunk[this.cx_size][this.cz_size];
        this.myColumns = new Column[this.x_size][this.z_size];

        for (int x = 0; x < this.x_size; x++) {
            for (int z = 0; z < this.z_size; z++) {
                this.myColumns[x][z] = new Column(w.getChunk(minX + x >> 4, minZ + z >> 4), minX + x & 0xF,
                        minZ + z & 0xF, minCY, cy_size);
            }
        }

        final IMovableRegistry mr = Api.instance().registries().movable();

        for (int cx = 0; cx < this.cx_size; cx++) {
            for (int cz = 0; cz < this.cz_size; cz++) {
                final List<BlockPos> deadBlockEntities = new ArrayList<>();

                final LevelChunk c = w.getChunk(minCX + cx, minCZ + cz);
                this.myChunks[cx][cz] = c;

                final List<Entry<BlockPos, BlockEntity>> rawBlockEntities = new ArrayList<>(c.getBlockEntities().entrySet());
                for (final Entry<BlockPos, BlockEntity> tx : rawBlockEntities) {
                    final BlockPos cp = tx.getKey();
                    final BlockEntity te = tx.getValue();

                    final BlockPos tePOS = te.getBlockPos();
                    if (tePOS.getX() >= minX && tePOS.getX() <= maxX && tePOS.getY() >= minY && tePOS.getY() <= maxY
                            && tePOS.getZ() >= minZ && tePOS.getZ() <= maxZ) {
                        if (mr.askToMove(te)) {
                            this.blockEntities.add(te);
                            deadBlockEntities.add(cp);
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

                for (final BlockPos cp : deadBlockEntities) {
                    c.getBlockEntities().remove(cp);
                }

                final long gameTime = this.getLevel().getGameTime();
                final ServerTickList<Block> pendingBlockTicks = this.getLevel().getBlockTicks();
                var pending = pendingBlockTicks.fetchTicksInChunk(c.getPos(), false, true);
                for (var entry : pending) {
                    final BlockPos tePOS = entry.pos;
                    if (tePOS.getX() >= minX && tePOS.getX() <= maxX && tePOS.getY() >= minY && tePOS.getY() <= maxY
                            && tePOS.getZ() >= minZ && tePOS.getZ() <= maxZ) {
                        this.ticks.add(new TickNextTickData<>(tePOS, entry.getType(),
                                entry.triggerTick - gameTime, entry.priority));
                    }
                }
            }
        }

        for (var te : this.blockEntities) {
            try {
                this.getLevel().removeBlockEntity(te.getBlockPos());
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

            for (final BlockEntity te : this.blockEntities) {
                final BlockPos tePOS = te.getBlockPos();
                dst.addBlockEntity(tePOS.getX() - this.x_offset, tePOS.getY() - this.y_offset, tePOS.getZ() - this.z_offset,
                        te, this, mr);
            }

            for (final BlockEntity te : dst.blockEntities) {
                final BlockPos tePOS = te.getBlockPos();
                this.addBlockEntity(tePOS.getX() - dst.x_offset, tePOS.getY() - dst.y_offset, tePOS.getZ() - dst.z_offset, te,
                        dst, mr);
            }

            for (final TickNextTickData<Block> entry : this.ticks) {
                final BlockPos tePOS = entry.pos;
                dst.addTick(tePOS.getX() - this.x_offset, tePOS.getY() - this.y_offset, tePOS.getZ() - this.z_offset,
                        entry);
            }

            for (final TickNextTickData<Block> entry : dst.ticks) {
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

    private void addTick(final int x, final int y, final int z, final TickNextTickData<Block> entry) {
        BlockPos where = new BlockPos(x + this.x_offset, y + this.y_offset, z + this.z_offset);
        this.level.getBlockTicks().scheduleTick(where, entry.getType(), (int) entry.triggerTick,
                entry.priority);
    }

    private void addBlockEntity(final int x, final int y, final int z, final BlockEntity te,
                                final CachedPlane alternateDestination, final IMovableRegistry mr) {
        try {
            final Column c = this.myColumns[x][z];

            if (c.doNotSkip(y + this.y_offset) || alternateDestination == null) {
                var handler = this.getHandler(te);

                var savedData = te.save(new CompoundTag());

                boolean success;
                try {
                    success = handler.moveBlockEntity(te, savedData, this.level,
                            new BlockPos(x + this.x_offset, y + this.y_offset, z + this.z_offset));
                } catch (final Throwable e) {
                    AELog.debug(e);
                    success = false;
                }

                if (!success) {
                    attemptRecovery(x, y, z, te, c, savedData);
                }

                mr.doneMoving(te);
            } else {
                alternateDestination.addBlockEntity(x, y, z, te, null, mr);
            }
        } catch (final Throwable e) {
            AELog.debug(e);
        }
    }

    private void attemptRecovery(int x, int y, int z, BlockEntity te, Column c, CompoundTag savedData) {
        final BlockPos pos = new BlockPos(x, y, z);
        AELog.debug("Trying to recover BE %s @ %s", BlockEntityType.getKey(te.getType()), pos);

        // attempt recovery, but do not reuse the same TE instance since we did destroy it
        var recoveredEntity = BlockEntity.loadStatic(pos, te.getBlockState(), savedData);
        if (recoveredEntity != null) {
            c.c.addAndRegisterBlockEntity(recoveredEntity);
            this.level.sendBlockUpdated(pos, this.level.getBlockState(pos), this.level.getBlockState(pos), z);
        } else {
            AELog.info("Failed to recover BE %s @ %s", BlockEntityType.getKey(te.getType()), pos);
        }
    }

    private void updateChunks() {

        LevelLightEngine lightManager = level.getLightEngine();

        // update shit..
        if (lightManager instanceof ThreadedLevelLightEngine serverLightManager) {
            for (int x = 0; x < this.cx_size; x++) {
                for (int z = 0; z < this.cz_size; z++) {
                    final LevelChunk c = this.myChunks[x][z];
                    serverLightManager.lightChunk(c, false);
                    c.markUnsaved();
                }
            }
        }

        // send shit...
        for (int x = 0; x < this.cx_size; x++) {
            for (int z = 0; z < this.cz_size; z++) {

                final LevelChunk c = this.myChunks[x][z];

                WorldData.instance().compassData().service().updateArea(this.getLevel(), c);

                ClientboundLevelChunkPacket cdp = new ClientboundLevelChunkPacket(c);
                level.getChunkSource().chunkMap.getPlayers(c.getPos(), false)
                        .forEach(spe -> spe.connection.send(cdp));
            }
        }

        // FIXME check if this makes any sense at all to send changes to players asap
        level.getChunkSource().tick(() -> false);
    }

    List<WorldCoord> getUpdates() {
        return this.updates;
    }

    ServerLevel getLevel() {
        return this.level;
    }

    private static class BlockStorageData {
        public BlockState state;
    }

    private class Column {
        private final int x;
        private final int z;
        private final LevelChunk c;
        private List<Integer> skipThese = null;

        public Column(final LevelChunk chunk, final int x, final int z, final int chunkY, final int chunkHeight) {
            this.x = x;
            this.z = z;
            this.c = chunk;

            final LevelChunkSection[] storage = this.c.getSections();

            // make sure storage exists before hand...
            for (int ay = 0; ay < chunkHeight; ay++) {
                final int by = ay + chunkY;
                LevelChunkSection extendedblockstorage = storage[by];
                if (extendedblockstorage == null) {
                    extendedblockstorage = storage[by] = new LevelChunkSection(by << 4);
                }
            }
        }

        private void setBlockState(final int y, BlockStorageData data) {
            if (data.state == CachedPlane.this.matrixBlockState) {
                data.state = Blocks.AIR.defaultBlockState();
            }
            final LevelChunkSection[] storage = this.c.getSections();
            final LevelChunkSection extendedBlockStorage = storage[y >> 4];
            extendedBlockStorage.setBlockState(this.x, y & 15, this.z, data.state);
        }

        private void fillData(final int y, BlockStorageData data) {
            final LevelChunkSection[] storage = this.c.getSections();
            final LevelChunkSection extendedblockstorage = storage[y >> 4];

            data.state = extendedblockstorage.getBlockState(this.x, y & 15, this.z);
        }

        private boolean doNotSkip(final int y) {
            final LevelChunkSection[] storage = this.c.getSections();
            final LevelChunkSection extendedblockstorage = storage[y >> 4];
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
