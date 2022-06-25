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

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import appeng.api.util.WorldCoord;
import appeng.core.definitions.AEBlocks;
import appeng.core.stats.AdvancementTriggers;

public class SpatialStorageHelper {

    private static SpatialStorageHelper instance;

    public static SpatialStorageHelper getInstance() {
        if (instance == null) {
            instance = new SpatialStorageHelper();
        }
        return instance;
    }

    /**
     * Mostly from dimensional doors.. which mostly got it form X-Comp.
     *
     * @param entity to be teleported entity
     * @param link   destination
     * @return teleported entity
     */
    private Entity teleportEntity(Entity entity, TelDestination link) {
        final ServerLevel oldLevel;
        final ServerLevel newLevel;

        try {
            oldLevel = (ServerLevel) entity.level;
            newLevel = link.dim;
        } catch (Throwable e) {
            return entity;
        }

        if (oldLevel == null) {
            return entity;
        }
        if (newLevel == null) {
            return entity;
        }
        if (newLevel == oldLevel) {
            // just set the location. Minecraft will handle eventual passengers
            newLevel.getChunkSource().getChunk(Mth.floor(link.x) >> 4, Mth.floor(link.z) >> 4,
                    ChunkStatus.FULL, true);
            entity.teleportTo(link.x, link.y, link.z);
            return entity;
        }

        // Are we riding something? Teleport it instead.
        if (entity.isPassenger()) {
            return this.teleportEntity(entity.getVehicle(), link);
        }

        // Is something riding us? Handle it first.
        final List<Entity> passengers = entity.getPassengers();
        final List<Entity> passengersOnOtherSide = new ArrayList<>(passengers.size());
        for (Entity passenger : passengers) {
            passenger.stopRiding();
            passengersOnOtherSide.add(this.teleportEntity(passenger, link));
        }
        // We keep track of all so we can remount them on the other side.

        // load the chunk!
        newLevel.getChunkSource().getChunk(Mth.floor(link.x) >> 4, Mth.floor(link.z) >> 4,
                ChunkStatus.FULL, true);

        if (entity instanceof ServerPlayer && link.dim.dimension() == SpatialStorageDimensionIds.WORLD_ID) {
            AdvancementTriggers.SPATIAL_EXPLORER.trigger((ServerPlayer) entity);
        }

        PortalInfo portalInfo = new PortalInfo(new Vec3(link.x, link.y, link.z), Vec3.ZERO, entity.getYRot(),
                entity.getXRot());
        entity = FabricDimensions.teleport(entity, link.dim, portalInfo);
        if (entity != null && !passengersOnOtherSide.isEmpty()) {
            for (Entity passanger : passengersOnOtherSide) {
                passanger.startRiding(entity, true);
            }
        }

        return entity;
    }

    private void transverseEdges(int minX, int minY, int minZ, int maxX, int maxY,
            int maxZ, ISpatialVisitor visitor) {
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

    public void swapRegions(ServerLevel srcLevel, int srcX, int srcY, int srcZ,
            ServerLevel dstLevel, int dstX, int dstY, int dstZ, int scaleX,
            int scaleY, int scaleZ) {
        Block matrixFrameBlock = AEBlocks.MATRIX_FRAME.block();
        this.transverseEdges(dstX - 1, dstY - 1, dstZ - 1, dstX + scaleX + 1,
                dstY + scaleY + 1, dstZ + scaleZ + 1,
                new WrapInMatrixFrame(matrixFrameBlock.defaultBlockState(), dstLevel));

        final AABB srcBox = new AABB(srcX, srcY, srcZ, srcX + scaleX + 1, srcY + scaleY + 1,
                srcZ + scaleZ + 1);

        final AABB dstBox = new AABB(dstX, dstY, dstZ, dstX + scaleX + 1, dstY + scaleY + 1,
                dstZ + scaleZ + 1);

        final CachedPlane cDst = new CachedPlane(dstLevel, dstX, dstY, dstZ, dstX + scaleX, dstY + scaleY,
                dstZ + scaleZ);
        final CachedPlane cSrc = new CachedPlane(srcLevel, srcX, srcY, srcZ, srcX + scaleX, srcY + scaleY,
                srcZ + scaleZ);

        // do nearly all the work... swaps blocks, block entities, and block ticks
        cSrc.swap(cDst);

        final List<Entity> srcE = srcLevel.getEntitiesOfClass(Entity.class, srcBox);
        final List<Entity> dstE = dstLevel.getEntitiesOfClass(Entity.class, dstBox);

        for (Entity e : dstE) {
            this.teleportEntity(e, new TelDestination(srcLevel, srcBox, e.getX(), e.getY(), e.getZ(),
                    -dstX + srcX, -dstY + srcY, -dstZ + srcZ));
        }

        for (Entity e : srcE) {
            this.teleportEntity(e, new TelDestination(dstLevel, dstBox, e.getX(), e.getY(), e.getZ(),
                    -srcX + dstX, -srcY + dstY, -srcZ + dstZ));
        }

        for (WorldCoord wc : cDst.getUpdates()) {
            cSrc.getLevel().updateNeighborsAt(wc.getPos(), Blocks.AIR);
        }

        for (WorldCoord wc : cSrc.getUpdates()) {
            cSrc.getLevel().updateNeighborsAt(wc.getPos(), Blocks.AIR);
        }

        this.transverseEdges(srcX - 1, srcY - 1, srcZ - 1, srcX + scaleX + 1, srcY + scaleY + 1, srcZ + scaleZ + 1,
                new TriggerUpdates(srcLevel));
        this.transverseEdges(dstX - 1, dstY - 1, dstZ - 1, dstX + scaleX + 1, dstY + scaleY + 1, dstZ + scaleZ + 1,
                new TriggerUpdates(dstLevel));

        this.transverseEdges(srcX, srcY, srcZ, srcX + scaleX, srcY + scaleY, srcZ + scaleZ,
                new TriggerUpdates(srcLevel));
        this.transverseEdges(dstX, dstY, dstZ, dstX + scaleX, dstY + scaleY, dstZ + scaleZ,
                new TriggerUpdates(dstLevel));
    }

    private static class TriggerUpdates implements ISpatialVisitor {

        private final Level dst;

        public TriggerUpdates(Level dst2) {
            this.dst = dst2;
        }

        @Override
        public void visit(BlockPos pos) {
            final BlockState state = this.dst.getBlockState(pos);
            final Block blk = state.getBlock();
            blk.neighborChanged(state, this.dst, pos, blk, pos, false);
        }
    }

    private static class WrapInMatrixFrame implements ISpatialVisitor {

        private final Level dst;
        private final BlockState state;

        public WrapInMatrixFrame(BlockState state, Level dst2) {
            this.dst = dst2;
            this.state = state;
        }

        @Override
        public void visit(BlockPos pos) {
            this.dst.setBlockAndUpdate(pos, this.state);
        }
    }

    private static class TelDestination {
        private final ServerLevel dim;
        private final double x;
        private final double y;
        private final double z;

        TelDestination(ServerLevel dimension, AABB srcBox, double x, double y,
                double z, int blockEntityX, int blockEntityY, int blockEntityZ) {
            this.dim = dimension;
            this.x = Math.min(srcBox.maxX - 0.5, Math.max(srcBox.minX + 0.5, x + blockEntityX));
            this.y = Math.min(srcBox.maxY - 0.5, Math.max(srcBox.minY + 0.5, y + blockEntityY));
            this.z = Math.min(srcBox.maxZ - 0.5, Math.max(srcBox.minZ + 0.5, z + blockEntityZ));
        }
    }

}
