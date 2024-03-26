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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ScheduledTick;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import appeng.api.ids.AETags;
import appeng.api.movable.BlockEntityMoveStrategies;
import appeng.api.movable.IBlockEntityMoveStrategy;
import appeng.core.AELog;
import appeng.core.definitions.AEBlocks;
import appeng.server.services.compass.CompassService;
import appeng.util.Platform;

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
    private final List<BlockEntityMoveRecord> blockEntities = new ArrayList<>();
    private final List<ScheduledTick<Block>> ticks = new ArrayList<>();
    private final ServerLevel level;
    private final List<BlockPos> updates = new ArrayList<>();
    private final BlockState matrixBlockState;

    public CachedPlane(ServerLevel level, int minX, int minY, int minZ, int maxX,
            int maxY, int maxZ) {

        Block matrixFrameBlock = AEBlocks.MATRIX_FRAME.block();
        if (matrixFrameBlock != null) {
            this.matrixBlockState = matrixFrameBlock.defaultBlockState();
        } else {
            this.matrixBlockState = null;
        }

        this.level = level;

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
                this.myColumns[x][z] = new Column(level.getChunk(minX + x >> 4, minZ + z >> 4), minX + x & 0xF,
                        minZ + z & 0xF);
            }
        }

        for (int cx = 0; cx < this.cx_size; cx++) {
            for (int cz = 0; cz < this.cz_size; cz++) {
                final LevelChunk c = level.getChunk(minCX + cx, minCZ + cz);
                this.myChunks[cx][cz] = c;

                // Make a copy of the BE list in the chunk. This allows us to immediately remove BE's we're moving.
                var rawBlockEntities = new ArrayList<>(c.getBlockEntities().entrySet());
                for (var entry : rawBlockEntities) {
                    var blockEntity = entry.getValue();

                    var pos = blockEntity.getBlockPos();
                    if (pos.getX() >= minX && pos.getX() <= maxX && pos.getY() >= minY && pos.getY() <= maxY
                            && pos.getZ() >= minZ && pos.getZ() <= maxZ) {

                        // If the block entities containing block is blacklisted, it will be skipped
                        // automatically later, so we have to avoid removing it here
                        if (blockEntity.getBlockState().is(AETags.SPATIAL_BLACKLIST)) {
                            continue;
                        }

                        var strategy = BlockEntityMoveStrategies.get(blockEntity);
                        var savedData = strategy.beginMove(blockEntity, level.registryAccess());
                        var section = c.getSection(c.getSectionIndex(entry.getKey().getY()));

                        // Coordinate within the section
                        int sx = entry.getKey().getX() & (LevelChunkSection.SECTION_WIDTH - 1);
                        int sy = entry.getKey().getY() & (LevelChunkSection.SECTION_HEIGHT - 1);
                        int sz = entry.getKey().getZ() & (LevelChunkSection.SECTION_WIDTH - 1);
                        var state = section.getBlockState(sx, sy, sz);

                        if (savedData != null) {
                            this.blockEntities.add(
                                    new BlockEntityMoveRecord(strategy, blockEntity, savedData, entry.getKey(), state));

                            // Set the state to AIR now since that prevents it from being resurrected recursively
                            section.setBlockState(sx, sy, sz, Blocks.AIR.defaultBlockState());
                            c.removeBlockEntity(entry.getKey());
                        } else {
                            // don't skip air, just let the code replace it...
                            if (state.isAir()) {
                                level.removeBlock(pos, false);
                            } else {
                                this.myColumns[pos.getX() - minX][pos.getZ() - minZ].setSkip(pos.getY());
                            }
                        }
                    }
                }

                var pending = (LevelChunkTicks<Block>) c.getBlockTicks();
                pending.getAll().forEach(entry -> {
                    var pos = entry.pos();
                    if (pos.getX() >= minX && pos.getX() <= maxX && pos.getY() >= minY && pos.getY() <= maxY
                            && pos.getZ() >= minZ && pos.getZ() <= maxZ) {
                        this.ticks.add(entry);
                    }
                });
            }
        }

    }

    void swap(CachedPlane dst) {
        if (dst.x_size == this.x_size && dst.y_size == this.y_size && dst.z_size == this.z_size) {
            AELog.info("Block Copy Scale: " + this.x_size + ", " + this.y_size + ", " + this.z_size);

            long startTime = System.nanoTime();

            for (int x = 0; x < this.x_size; x++) {
                for (int z = 0; z < this.z_size; z++) {
                    final Column srcCol = this.myColumns[x][z];
                    final Column dstCol = dst.myColumns[x][z];

                    for (int y = 0; y < this.y_size; y++) {
                        var src_y = this.y_offset + y;
                        var dst_y = dst.y_offset + y;

                        if (srcCol.doNotSkip(src_y) && dstCol.doNotSkip(dst_y)) {
                            var srcSection = srcCol.getSection(src_y);
                            var dstSection = dstCol.getSection(dst_y);

                            var srcState = srcSection.getBlockState(srcCol.x, SectionPos.sectionRelative(src_y),
                                    srcCol.z);
                            if (srcState == CachedPlane.this.matrixBlockState) {
                                srcState = Blocks.AIR.defaultBlockState();
                            }
                            var dstState = dstSection.getBlockState(dstCol.x, SectionPos.sectionRelative(dst_y),
                                    dstCol.z);
                            if (dstState == CachedPlane.this.matrixBlockState) {
                                dstState = Blocks.AIR.defaultBlockState();
                            }

                            srcSection.setBlockState(srcCol.x, SectionPos.sectionRelative(src_y), srcCol.z, dstState);
                            dstSection.setBlockState(dstCol.x, SectionPos.sectionRelative(dst_y), dstCol.z, srcState);
                        } else {
                            this.markForUpdate(this.x_offset + x, src_y, this.z_offset + z);
                            dst.markForUpdate(dst.x_offset + x, dst_y, dst.z_offset + z);
                        }
                    }
                }
            }

            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            AELog.info("Block Copy Time: " + duration);

            for (var moveRecord : this.blockEntities) {
                var pos = moveRecord.blockEntity().getBlockPos();
                dst.addBlockEntity(pos.getX() - this.x_offset, pos.getY() - this.y_offset,
                        pos.getZ() - this.z_offset,
                        moveRecord);
            }

            for (var moveRecord : dst.blockEntities) {
                var pos = moveRecord.blockEntity().getBlockPos();
                this.addBlockEntity(pos.getX() - dst.x_offset, pos.getY() - dst.y_offset,
                        pos.getZ() - dst.z_offset, moveRecord);
            }

            for (var entry : this.ticks) {
                var movedPos = entry.pos().offset(-this.x_offset, -this.y_offset, -this.z_offset);
                dst.addTick(movedPos, entry);
            }

            for (var entry : dst.ticks) {
                var movedPos = entry.pos().offset(-dst.x_offset, -dst.y_offset, -dst.z_offset);
                addTick(movedPos, entry);
            }

            startTime = System.nanoTime();
            this.updateChunks();
            dst.updateChunks();
            endTime = System.nanoTime();

            duration = endTime - startTime;
            AELog.info("Update Time: " + duration);
        }
    }

    private void markForUpdate(int x, int y, int z) {
        this.updates.add(new BlockPos(x, y, z));
        for (Direction d : Direction.values()) {
            this.updates.add(new BlockPos(x + d.getStepX(), y + d.getStepY(), z + d.getStepZ()));
        }
    }

    private void addTick(BlockPos pos, ScheduledTick<Block> tick) {
        this.level.getBlockTicks().schedule(new ScheduledTick<>(
                tick.type(), pos, tick.triggerTick(), tick.priority(), tick.subTickOrder()));
    }

    private void addBlockEntity(int x, int y, int z, BlockEntityMoveRecord moveRecord) {
        try {
            var originalPos = moveRecord.pos();

            var c = this.myColumns[x][z];
            if (!c.doNotSkip(y + this.y_offset)) {
                AELog.warn(
                        "Block entity %s was queued to be moved from %s, but it's position then skipped during the move.",
                        moveRecord.blockEntity(), originalPos);
                return;
            }

            var newPosition = new BlockPos(x + this.x_offset, y + this.y_offset, z + this.z_offset);

            // Restore original block state to the section directly. The chunk would create the BE and notify neighbors.
            var chunk = this.level.getChunk(newPosition);
            var section = chunk.getSection(chunk.getSectionIndex(newPosition.getY()));
            section.setBlockState(
                    newPosition.getX() & (LevelChunkSection.SECTION_WIDTH - 1),
                    newPosition.getY() & (LevelChunkSection.SECTION_HEIGHT - 1),
                    newPosition.getZ() & (LevelChunkSection.SECTION_WIDTH - 1),
                    moveRecord.state);

            var strategy = moveRecord.strategy();
            boolean success;
            try {
                success = strategy.completeMove(moveRecord.blockEntity(), moveRecord.state(), moveRecord.savedData(),
                        this.level,
                        newPosition);
            } catch (Throwable e) {
                AELog.warn(e);
                success = false;
            }

            if (!success) {
                attemptRecovery(x, y, z, moveRecord, c);
            }
        } catch (Throwable e) {
            AELog.warn(e);
        }
    }

    private void attemptRecovery(int x, int y, int z, BlockEntityMoveRecord moveRecord, Column c) {
        var pos = new BlockPos(x, y, z);
        var type = moveRecord.blockEntity().getType();
        AELog.debug("Trying to recover BE %s @ %s", BlockEntityType.getKey(type), pos);

        // attempt recovery, but do not reuse the same TE instance since we did destroy it
        var blockState = moveRecord.blockEntity().getBlockState();
        var recoveredEntity = BlockEntity.loadStatic(pos, blockState, moveRecord.savedData(), level.registryAccess());
        if (recoveredEntity != null) {
            // We need to restore the block state too before re-setting the entity
            this.level.setBlock(pos, blockState, 3);
            c.c.addAndRegisterBlockEntity(recoveredEntity);
            this.level.sendBlockUpdated(pos, this.level.getBlockState(pos), this.level.getBlockState(pos), z);
        } else {
            AELog.warn("Failed to recover BE %s @ %s", BlockEntityType.getKey(type), pos);
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
                    c.setUnsaved(true);
                }
            }
        }

        // send shit...
        for (int x = 0; x < this.cx_size; x++) {
            for (int z = 0; z < this.cz_size; z++) {

                final LevelChunk c = this.myChunks[x][z];

                CompassService.updateArea(this.getLevel(), c);

                var cdp = Platform.getFullChunkPacket(c);
                level.getChunkSource().chunkMap.getPlayers(c.getPos(), false)
                        .forEach(spe -> spe.connection.send(cdp));
            }
        }

        // FIXME check if this makes any sense at all to send changes to players asap
        level.getChunkSource().tick(() -> false, false);
    }

    List<BlockPos> getUpdates() {
        return this.updates;
    }

    ServerLevel getLevel() {
        return this.level;
    }

    private static class BlockStorageData {
        public BlockState state;
    }

    private static class Column {
        private final int x;
        private final int z;

        private final LevelChunk c;
        private List<Integer> skipThese = null;
        private Int2ObjectMap<BlockState> savedBlockStates = null;

        public Column(LevelChunk chunk, int x, int z) {
            this.x = x;
            this.z = z;
            this.c = chunk;
        }

        private boolean doNotSkip(int y) {
            var blockState = getSection(y).getBlockState(this.x, SectionPos.sectionRelative(y), this.z);
            if (blockState.is(AETags.SPATIAL_BLACKLIST)) {
                return false;
            }

            return this.skipThese == null || !this.skipThese.contains(y);
        }

        private void setSkip(int y) {
            if (this.skipThese == null) {
                this.skipThese = new ArrayList<>();
            }
            this.skipThese.add(y);
        }

        public LevelChunkSection getSection(int y) {
            return c.getSection(c.getSectionIndexFromSectionY(SectionPos.blockToSectionCoord(y)));
        }
    }

    private record BlockEntityMoveRecord(
            IBlockEntityMoveStrategy strategy,
            BlockEntity blockEntity,
            CompoundTag savedData,
            BlockPos pos,
            BlockState state) {
    }

}
