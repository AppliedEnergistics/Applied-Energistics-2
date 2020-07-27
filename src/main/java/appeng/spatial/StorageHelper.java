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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;

import appeng.api.util.WorldCoord;
import appeng.core.Api;
import appeng.core.AppEng;

public class StorageHelper {

    private static StorageHelper instance;

    public static StorageHelper getInstance() {
        if (instance == null) {
            instance = new StorageHelper();
        }
        return instance;
    }

    /**
     * Mostly from dimensional doors.. which mostly got it form X-Comp.
     *
     * @param entity to be teleported entity
     * @param link   destination
     *
     * @return teleported entity
     */
    private Entity teleportEntity(Entity entity, final TelDestination link) {
        final ServerWorld oldWorld;
        final ServerWorld newWorld;

        try {
            oldWorld = (ServerWorld) entity.world;
            newWorld = link.dim;
        } catch (final Throwable e) {
            return entity;
        }

        if (oldWorld == null) {
            return entity;
        }
        if (newWorld == null) {
            return entity;
        }
        if (newWorld == oldWorld) {
            return entity;
        }

        // Are we riding something? Teleport it instead.
        if (entity.hasVehicle()) {
            return this.teleportEntity(entity.getVehicle(), link);
        }

        // Is something riding us? Handle it first.
        final List<Entity> passengers = entity.getPassengerList();
        final List<Entity> passengersOnOtherSide = new ArrayList<>(passengers.size());
        for (Entity passenger : passengers) {
            passenger.stopRiding();
            passengersOnOtherSide.add(this.teleportEntity(passenger, link));
        }
        // We keep track of all so we can remount them on the other side.

        // load the chunk!
        newWorld.getChunkManager().getChunk(MathHelper.floor(link.x) >> 4, MathHelper.floor(link.z) >> 4,
                ChunkStatus.FULL, true);

        if (entity instanceof ServerPlayerEntity
                && link.dim.getDimensionRegistryKey().equals(SpatialDimensionManager.STORAGE_DIMENSION_TYPE)) {
            AppEng.instance().getAdvancementTriggers().getSpatialExplorer().trigger((ServerPlayerEntity) entity);
        }

        // FIXME FABRIC new METeleporter(link)
        float yaw = entity.yaw;
        entity = entity.changeDimension(link.dim);
        entity.yaw = yaw;
        entity.refreshPositionAfterTeleport(link.x, link.y, link.z);
        entity.setVelocity(0, 0, 0);

        if (!passengersOnOtherSide.isEmpty()) {
            for (Entity passanger : passengersOnOtherSide) {
                passanger.startRiding(entity, true);
            }
        }

        return entity;
    }

    private void transverseEdges(final int minX, final int minY, final int minZ, final int maxX, final int maxY,
            final int maxZ, final ISpatialVisitor visitor) {
        for (int y = minY; y < maxY; y++) {
            for (int z = minZ; z < maxZ; z++) {
                visitor.visit(new BlockPos(minX, y, z));
                visitor.visit(new BlockPos(maxX, y, z));
            }
        }

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                visitor.visit(new BlockPos(x, minY, z));
                visitor.visit(new BlockPos(x, maxY, z));
            }
        }

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                visitor.visit(new BlockPos(x, y, minZ));
                visitor.visit(new BlockPos(x, y, maxZ));
            }
        }
    }

    public void swapRegions(final ServerWorld srcWorld, final int srcX, final int srcY, final int srcZ,
            final ServerWorld dstWorld, final int dstX, final int dstY, final int dstZ, final int scaleX,
            final int scaleY, final int scaleZ) {
        Api.instance().definitions().blocks().matrixFrame().maybeBlock()
                .ifPresent(matrixFrameBlock -> this.transverseEdges(dstX - 1, dstY - 1, dstZ - 1, dstX + scaleX + 1,
                        dstY + scaleY + 1, dstZ + scaleZ + 1,
                        new WrapInMatrixFrame(matrixFrameBlock.getDefaultState(), dstWorld)));

        final Box srcBox = new Box(srcX, srcY, srcZ, srcX + scaleX + 1, srcY + scaleY + 1, srcZ + scaleZ + 1);

        final Box dstBox = new Box(dstX, dstY, dstZ, dstX + scaleX + 1, dstY + scaleY + 1, dstZ + scaleZ + 1);

        final CachedPlane cDst = new CachedPlane(dstWorld, dstX, dstY, dstZ, dstX + scaleX, dstY + scaleY,
                dstZ + scaleZ);
        final CachedPlane cSrc = new CachedPlane(srcWorld, srcX, srcY, srcZ, srcX + scaleX, srcY + scaleY,
                srcZ + scaleZ);

        // do nearly all the work... swaps blocks, tiles, and block ticks
        cSrc.swap(cDst);

        final List<Entity> srcE = srcWorld.getEntitiesIncludingUngeneratedChunks(Entity.class, srcBox);
        final List<Entity> dstE = dstWorld.getEntitiesIncludingUngeneratedChunks(Entity.class, dstBox);

        for (final Entity e : dstE) {
            this.teleportEntity(e, new TelDestination(srcWorld, srcBox, e.getX(), e.getY(), e.getZ(), -dstX + srcX,
                    -dstY + srcY, -dstZ + srcZ));
        }

        for (final Entity e : srcE) {
            this.teleportEntity(e, new TelDestination(dstWorld, dstBox, e.getX(), e.getY(), e.getZ(), -srcX + dstX,
                    -srcY + dstY, -srcZ + dstZ));
        }

        for (final WorldCoord wc : cDst.getUpdates()) {
            cSrc.getWorld().updateNeighborsAlways(wc.getPos(), Blocks.AIR);
        }

        for (final WorldCoord wc : cSrc.getUpdates()) {
            cSrc.getWorld().updateNeighborsAlways(wc.getPos(), Blocks.AIR);
        }

        this.transverseEdges(srcX - 1, srcY - 1, srcZ - 1, srcX + scaleX + 1, srcY + scaleY + 1, srcZ + scaleZ + 1,
                new TriggerUpdates(srcWorld));
        this.transverseEdges(dstX - 1, dstY - 1, dstZ - 1, dstX + scaleX + 1, dstY + scaleY + 1, dstZ + scaleZ + 1,
                new TriggerUpdates(dstWorld));

        this.transverseEdges(srcX, srcY, srcZ, srcX + scaleX, srcY + scaleY, srcZ + scaleZ,
                new TriggerUpdates(srcWorld));
        this.transverseEdges(dstX, dstY, dstZ, dstX + scaleX, dstY + scaleY, dstZ + scaleZ,
                new TriggerUpdates(dstWorld));
    }

    private static class TriggerUpdates implements ISpatialVisitor {

        private final World dst;

        public TriggerUpdates(final World dst2) {
            this.dst = dst2;
        }

        @Override
        public void visit(final BlockPos pos) {
            final BlockState state = this.dst.getBlockState(pos);
            final Block blk = state.getBlock();
            blk.neighborUpdate(state, this.dst, pos, blk, pos, false);
        }
    }

    private static class WrapInMatrixFrame implements ISpatialVisitor {

        private final World dst;
        private final BlockState state;

        public WrapInMatrixFrame(final BlockState state, final World dst2) {
            this.dst = dst2;
            this.state = state;
        }

        @Override
        public void visit(final BlockPos pos) {
            this.dst.setBlockState(pos, this.state);
        }
    }

    private static class TelDestination {
        private final ServerWorld dim;
        private final double x;
        private final double y;
        private final double z;

        TelDestination(final ServerWorld dimension, final Box srcBox, final double x, final double y, final double z,
                final int tileX, final int tileY, final int tileZ) {
            this.dim = dimension;
            this.x = Math.min(srcBox.maxX - 0.5, Math.max(srcBox.minX + 0.5, x + tileX));
            this.y = Math.min(srcBox.maxY - 0.5, Math.max(srcBox.minY + 0.5, y + tileY));
            this.z = Math.min(srcBox.maxZ - 0.5, Math.max(srcBox.minZ + 0.5, z + tileZ));
        }
    }

}
