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


import appeng.api.AEApi;
import appeng.api.movable.IMovableHandler;
import appeng.api.movable.IMovableRegistry;
import appeng.api.util.AEPartLocation;
import appeng.api.util.WorldCoord;
import appeng.core.AELog;
import appeng.core.worlddata.WorldData;
import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


public class CachedPlane {
    private final int x_size;
    private final int z_size;
    private final int cx_size;
    private final int cz_size;
    private final int x_offset;
    private final int y_offset;
    private final int z_offset;
    private final int y_size;
    private final Chunk[][] myChunks;
    private final Column[][] myColumns;
    private final List<TileEntity> tiles = new ArrayList<>();
    private final List<NextTickListEntry> ticks = new ArrayList<>();
    private final World world;
    private final IMovableRegistry reg = AEApi.instance().registries().movable();
    private final List<WorldCoord> updates = new ArrayList<>();
    private int verticalBits;
    private final IBlockState matrixBlockState;

    public CachedPlane(final World w, final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ) {

        Block matrixFrameBlock = AEApi.instance().definitions().blocks().matrixFrame().maybeBlock().orElse(null);
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

        this.myChunks = new Chunk[this.cx_size][this.cz_size];
        this.myColumns = new Column[this.x_size][this.z_size];

        this.verticalBits = 0;
        for (int cy = 0; cy < cy_size; cy++) {
            this.verticalBits |= 1 << (minCY + cy);
        }

        for (int x = 0; x < this.x_size; x++) {
            for (int z = 0; z < this.z_size; z++) {
                this.myColumns[x][z] = new Column(w.getChunkFromChunkCoords((minX + x) >> 4,
                        (minZ + z) >> 4), (minX + x) & 0xF, (minZ + z) & 0xF, minCY, cy_size);
            }
        }

        final IMovableRegistry mr = AEApi.instance().registries().movable();

        for (int cx = 0; cx < this.cx_size; cx++) {
            for (int cz = 0; cz < this.cz_size; cz++) {
                final List<Entry<BlockPos, TileEntity>> rawTiles = new ArrayList<>();
                final List<BlockPos> deadTiles = new ArrayList<>();

                final Chunk c = w.getChunkFromChunkCoords(minCX + cx, minCZ + cz);
                this.myChunks[cx][cz] = c;

                rawTiles.addAll(c.getTileEntityMap().entrySet());
                for (final Entry<BlockPos, TileEntity> tx : rawTiles) {
                    final BlockPos cp = tx.getKey();
                    final TileEntity te = tx.getValue();

                    final BlockPos tePOS = te.getPos();
                    if (tePOS.getX() >= minX && tePOS.getX() <= maxX && tePOS.getY() >= minY && tePOS.getY() <= maxY && tePOS.getZ() >= minZ && tePOS
                            .getZ() <= maxZ) {
                        if (mr.askToMove(te)) {
                            this.tiles.add(te);
                            deadTiles.add(cp);
                        } else {
                            final BlockStorageData details = new BlockStorageData();
                            this.myColumns[tePOS.getX() - minX][tePOS.getZ() - minZ].fillData(tePOS.getY(), details);

                            // don't skip air, just let the code replace it...
                            if (details.state != null && details.state.getBlock() == Platform.AIR_BLOCK && details.state.getMaterial().isReplaceable()) {
                                w.setBlockToAir(tePOS);
                            } else {
                                this.myColumns[tePOS.getX() - minX][tePOS.getZ() - minZ].setSkip(tePOS.getY());
                            }
                        }
                    }
                }

                for (final BlockPos cp : deadTiles) {
                    c.getTileEntityMap().remove(cp);
                }

                final long k = this.getWorld().getTotalWorldTime();
                final List<NextTickListEntry> list = this.getWorld().getPendingBlockUpdates(c, false);
                if (list != null) {
                    for (final NextTickListEntry entry : list) {
                        final BlockPos tePOS = entry.position;
                        if (tePOS.getX() >= minX && tePOS.getX() <= maxX && tePOS.getY() >= minY && tePOS.getY() <= maxY && tePOS.getZ() >= minZ && tePOS
                                .getZ() <= maxZ) {
                            final NextTickListEntry newEntry = new NextTickListEntry(tePOS, entry.getBlock());
                            newEntry.scheduledTime = entry.scheduledTime - k;
                            this.ticks.add(newEntry);
                        }
                    }
                }
            }
        }

        for (final TileEntity te : this.tiles) {
            try {
                this.getWorld().loadedTileEntityList.remove(te);
                if (te instanceof ITickable) {
                    this.getWorld().tickableTileEntities.remove(te);
                }
            } catch (final Exception e) {
                AELog.debug(e);
            }
        }
    }

    private IMovableHandler getHandler(final TileEntity te) {
        final IMovableRegistry mr = AEApi.instance().registries().movable();
        return mr.getHandler(te);
    }

    void swap(final CachedPlane dst) {
        final IMovableRegistry mr = AEApi.instance().registries().movable();

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

                            a.setBlockIDWithMetadata(src_y, bD);
                            b.setBlockIDWithMetadata(dst_y, aD);
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

            for (final TileEntity te : this.tiles) {
                final BlockPos tePOS = te.getPos();
                dst.addTile(tePOS.getX() - this.x_offset, tePOS.getY() - this.y_offset, tePOS.getZ() - this.z_offset, te, this, mr);
            }

            for (final TileEntity te : dst.tiles) {
                final BlockPos tePOS = te.getPos();
                this.addTile(tePOS.getX() - dst.x_offset, tePOS.getY() - dst.y_offset, tePOS.getZ() - dst.z_offset, te, dst, mr);
            }

            for (final NextTickListEntry entry : this.ticks) {
                final BlockPos tePOS = entry.position;
                dst.addTick(tePOS.getX() - this.x_offset, tePOS.getY() - this.y_offset, tePOS.getZ() - this.z_offset, entry);
            }

            for (final NextTickListEntry entry : dst.ticks) {
                final BlockPos tePOS = entry.position;
                this.addTick(tePOS.getX() - dst.x_offset, tePOS.getY() - dst.y_offset, tePOS.getZ() - dst.z_offset, entry);
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

    private void addTick(final int x, final int y, final int z, final NextTickListEntry entry) {
        this.world.scheduleUpdate(new BlockPos(x + this.x_offset, y + this.y_offset, z + this.z_offset), entry.getBlock(), (int) entry.scheduledTime);
    }

    private void addTile(final int x, final int y, final int z, final TileEntity te, final CachedPlane alternateDestination, final IMovableRegistry mr) {
        try {
            final Column c = this.myColumns[x][z];

            if (c.doNotSkip(y + this.y_offset) || alternateDestination == null) {
                final IMovableHandler handler = this.getHandler(te);

                try {
                    handler.moveTile(te, this.world, new BlockPos(x + this.x_offset, y + this.y_offset, z + this.z_offset));
                } catch (final Throwable e) {
                    AELog.debug(e);

                    final BlockPos pos = new BlockPos(x, y, z);

                    // attempt recovery...
                    te.setWorld(this.world);
                    te.setPos(pos);
                    c.c.addTileEntity(new BlockPos(c.x, y + y, c.z), te);
                    // c.c.setChunkTileEntity( c.x, y + y, c.z, te );

                    if (c.c.isLoaded()) {
                        this.world.addTileEntity(te);
                        this.world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), z);
                    }
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

        // update shit..
        for (int x = 0; x < this.cx_size; x++) {
            for (int z = 0; z < this.cz_size; z++) {
                final Chunk c = this.myChunks[x][z];
                c.resetRelightChecks();
                c.generateSkylightMap();
                c.setModified(true);
            }
        }

        // send shit...
        for (int x = 0; x < this.cx_size; x++) {
            for (int z = 0; z < this.cz_size; z++) {

                final Chunk c = this.myChunks[x][z];

                for (int y = 1; y < 255; y += 32) {
                    WorldData.instance().compassData().service().updateArea(this.getWorld(), c.x << 4, y, c.z << 4);
                }

                Platform.sendChunk(c, this.verticalBits);
            }
        }
    }

    List<WorldCoord> getUpdates() {
        return this.updates;
    }

    World getWorld() {
        return this.world;
    }

    private static class BlockStorageData {
        public IBlockState state;
        public int light;
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

            final ExtendedBlockStorage[] storage = this.c.getBlockStorageArray();

            // make sure storage exists before hand...
            for (int ay = 0; ay < chunkHeight; ay++) {
                final int by = (ay + chunkY);
                ExtendedBlockStorage extendedblockstorage = storage[by];
                if (extendedblockstorage == null) {
                    extendedblockstorage = storage[by] = new ExtendedBlockStorage(by << 4, this.c.getWorld().provider.hasSkyLight());
                }
            }
        }

        private void setBlockIDWithMetadata(final int y, BlockStorageData data) {
            if (data.state == CachedPlane.this.matrixBlockState) {
                data.state = Platform.AIR_BLOCK.getDefaultState();
            }
            final ExtendedBlockStorage[] storage = this.c.getBlockStorageArray();
            final ExtendedBlockStorage extendedBlockStorage = storage[y >> 4];
            extendedBlockStorage.set(this.x, y & 15, this.z, data.state);
            extendedBlockStorage.setBlockLight(this.x, y & 15, this.z, data.light);
        }

        private void fillData(final int y, BlockStorageData data) {
            final ExtendedBlockStorage[] storage = this.c.getBlockStorageArray();
            final ExtendedBlockStorage extendedblockstorage = storage[y >> 4];

            data.state = extendedblockstorage.get(this.x, y & 15, this.z);
            data.light = extendedblockstorage.getBlockLight(this.x, y & 15, this.z);
        }

        private boolean doNotSkip(final int y) {
            final ExtendedBlockStorage[] storage = this.c.getBlockStorageArray();
            final ExtendedBlockStorage extendedblockstorage = storage[y >> 4];
            if (CachedPlane.this.reg.isBlacklisted(extendedblockstorage.get(this.x, y & 15, this.z).getBlock())) {
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
