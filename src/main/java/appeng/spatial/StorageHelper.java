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
import appeng.api.util.WorldCoord;
import appeng.core.AppEng;
import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;

import java.util.ArrayList;
import java.util.List;


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
     * @return teleported entity
     */
    private Entity teleportEntity(Entity entity, final TelDestination link) {
        final WorldServer oldWorld;
        final WorldServer newWorld;

        try {
            oldWorld = (WorldServer) entity.world;
            newWorld = (WorldServer) link.dim;
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
        if (entity.isRiding()) {
            return this.teleportEntity(entity.getRidingEntity(), link);
        }

        // Is something riding us? Handle it first.
        final List<Entity> passangers = entity.getPassengers();
        final List<Entity> passangersOnOtherSide = new ArrayList<>();
        if (!passangers.isEmpty()) {
            for (Entity passanger : passangers) {
                passanger.dismountRidingEntity();
                passangersOnOtherSide.add(this.teleportEntity(passanger, link));
            }
            // We keep track of all so we can remount them on the other side.
        }

        // load the chunk!
        newWorld.getChunkProvider().provideChunk(MathHelper.floor(link.x) >> 4, MathHelper.floor(link.z) >> 4);

        if (entity instanceof EntityPlayerMP && link.dim.provider instanceof StorageWorldProvider) {
            AppEng.instance().getAdvancementTriggers().getSpatialExplorer().trigger((EntityPlayerMP) entity);
        }

        entity.changeDimension(link.dim.provider.getDimension(), new METeleporter(link));

        if (!passangersOnOtherSide.isEmpty()) {
            for (Entity passanger : passangersOnOtherSide) {
                passanger.startRiding(entity, true);
            }
        }

        return entity;
    }

    private void transverseEdges(final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ, final ISpatialVisitor visitor) {
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

    public void swapRegions(final World srcWorld, final int srcX, final int srcY, final int srcZ, final World dstWorld, final int dstX, final int dstY, final int dstZ, final int scaleX, final int scaleY, final int scaleZ) {
        AEApi.instance()
                .definitions()
                .blocks()
                .matrixFrame()
                .maybeBlock()
                .ifPresent(matrixFrameBlock -> this.transverseEdges(dstX - 1, dstY - 1, dstZ - 1,
                        dstX + scaleX + 1, dstY + scaleY + 1, dstZ + scaleZ + 1, new WrapInMatrixFrame(matrixFrameBlock.getDefaultState(), dstWorld)));

        final AxisAlignedBB srcBox = new AxisAlignedBB(srcX, srcY, srcZ, srcX + scaleX + 1, srcY + scaleY + 1, srcZ + scaleZ + 1);

        final AxisAlignedBB dstBox = new AxisAlignedBB(dstX, dstY, dstZ, dstX + scaleX + 1, dstY + scaleY + 1, dstZ + scaleZ + 1);

        final CachedPlane cDst = new CachedPlane(dstWorld, dstX, dstY, dstZ, dstX + scaleX, dstY + scaleY, dstZ + scaleZ);
        final CachedPlane cSrc = new CachedPlane(srcWorld, srcX, srcY, srcZ, srcX + scaleX, srcY + scaleY, srcZ + scaleZ);

        // do nearly all the work... swaps blocks, tiles, and block ticks
        cSrc.swap(cDst);

        final List<Entity> srcE = srcWorld.getEntitiesWithinAABB(Entity.class, srcBox);
        final List<Entity> dstE = dstWorld.getEntitiesWithinAABB(Entity.class, dstBox);

        for (final Entity e : dstE) {
            this.teleportEntity(e, new TelDestination(srcWorld, srcBox, e.posX, e.posY, e.posZ, -dstX + srcX, -dstY + srcY, -dstZ + srcZ));
        }

        for (final Entity e : srcE) {
            this.teleportEntity(e, new TelDestination(dstWorld, dstBox, e.posX, e.posY, e.posZ, -srcX + dstX, -srcY + dstY, -srcZ + dstZ));
        }

        for (final WorldCoord wc : cDst.getUpdates()) {
            cSrc.getWorld().notifyNeighborsOfStateChange(wc.getPos(), Platform.AIR_BLOCK, true);
        }

        for (final WorldCoord wc : cSrc.getUpdates()) {
            cSrc.getWorld().notifyNeighborsOfStateChange(wc.getPos(), Platform.AIR_BLOCK, true);
        }

        this.transverseEdges(srcX - 1, srcY - 1, srcZ - 1, srcX + scaleX + 1, srcY + scaleY + 1, srcZ + scaleZ + 1, new TriggerUpdates(srcWorld));
        this.transverseEdges(dstX - 1, dstY - 1, dstZ - 1, dstX + scaleX + 1, dstY + scaleY + 1, dstZ + scaleZ + 1, new TriggerUpdates(dstWorld));

        this.transverseEdges(srcX, srcY, srcZ, srcX + scaleX, srcY + scaleY, srcZ + scaleZ, new TriggerUpdates(srcWorld));
        this.transverseEdges(dstX, dstY, dstZ, dstX + scaleX, dstY + scaleY, dstZ + scaleZ, new TriggerUpdates(dstWorld));

        /*
         * IChunkProvider cp = destination.getChunkProvider(); if ( cp instanceof ChunkProviderServer ) {
         * ChunkProviderServer
         * srv = (ChunkProviderServer) cp; srv.unloadAllChunks(); }
         * cp.unloadQueuedChunks();
         */

    }

    private static class TriggerUpdates implements ISpatialVisitor {

        private final World dst;

        public TriggerUpdates(final World dst2) {
            this.dst = dst2;
        }

        @Override
        public void visit(final BlockPos pos) {
            final IBlockState state = this.dst.getBlockState(pos);
            final Block blk = state.getBlock();
            blk.neighborChanged(state, this.dst, pos, blk, pos);
        }
    }

    private static class WrapInMatrixFrame implements ISpatialVisitor {

        private final World dst;
        private final IBlockState state;

        public WrapInMatrixFrame(final IBlockState state, final World dst2) {
            this.dst = dst2;
            this.state = state;
        }

        @Override
        public void visit(final BlockPos pos) {
            this.dst.setBlockState(pos, this.state);
        }
    }

    private static class TelDestination {
        private final World dim;
        private final double x;
        private final double y;
        private final double z;

        TelDestination(final World dimension, final AxisAlignedBB srcBox, final double x, final double y, final double z, final int tileX, final int tileY, final int tileZ) {
            this.dim = dimension;
            this.x = Math.min(srcBox.maxX - 0.5, Math.max(srcBox.minX + 0.5, x + tileX));
            this.y = Math.min(srcBox.maxY - 0.5, Math.max(srcBox.minY + 0.5, y + tileY));
            this.z = Math.min(srcBox.maxZ - 0.5, Math.max(srcBox.minZ + 0.5, z + tileZ));
        }
    }

    private static class METeleporter implements ITeleporter {

        private final TelDestination destination;

        METeleporter(final TelDestination d) {
            this.destination = d;
        }

        @Override
        public void placeEntity(World world, Entity entity, float yaw) {
            entity.setLocationAndAngles(this.destination.x, this.destination.y, this.destination.z, yaw, entity.rotationPitch);
            entity.motionX = entity.motionY = entity.motionZ = 0.0D;
        }
    }
}
