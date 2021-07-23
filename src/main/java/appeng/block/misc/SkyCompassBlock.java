/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.block.misc;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

import appeng.block.AEBaseTileBlock;
import appeng.tile.misc.SkyCompassTileEntity;
import net.minecraft.world.phys.shapes.CollisionContext;

public class SkyCompassBlock extends AEBaseTileBlock<SkyCompassTileEntity> {

    public SkyCompassBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    public boolean isValidOrientation(final LevelAccessor w, final BlockPos pos, final Direction forward, final Direction up) {
        final SkyCompassTileEntity sc = this.getTileEntity(w, pos);
        if (sc != null) {
            return false;
        }
        return this.canPlaceAt(w, pos, forward.getOpposite());
    }

    private boolean canPlaceAt(final BlockGetter w, final BlockPos pos, final Direction dir) {
        final BlockPos test = pos.relative(dir);
        BlockState blockstate = w.getBlockState(test);
        return blockstate.isFaceSturdy(w, test, dir.getOpposite());
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos,
                                boolean isMoving) {
        final SkyCompassTileEntity sc = this.getTileEntity(world, pos);
        final Direction forward = sc.getForward();
        if (!this.canPlaceAt(world, pos, forward.getOpposite())) {
            this.dropTorch(world, pos);
        }
    }

    private void dropTorch(final Level w, final BlockPos pos) {
        final BlockState prev = w.getBlockState(pos);
        w.destroyBlock(pos, true);
        w.sendBlockUpdated(pos, prev, w.getBlockState(pos), 3);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader w, BlockPos pos) {
        for (final Direction dir : Direction.values()) {
            if (this.canPlaceAt(w, pos, dir)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter w, BlockPos pos, CollisionContext context) {

        // TODO: This definitely needs to be memoized

        final SkyCompassTileEntity tile = this.getTileEntity(w, pos);
        if (tile != null) {
            final Direction forward = tile.getForward();

            double minX = 0;
            double minY = 0;
            double minZ = 0;
            double maxX = 1;
            double maxY = 1;
            double maxZ = 1;

            switch (forward) {
                case DOWN:
                    minZ = minX = 5.0 / 16.0;
                    maxZ = maxX = 11.0 / 16.0;
                    maxY = 1.0;
                    minY = 14.0 / 16.0;
                    break;
                case EAST:
                    minZ = minY = 5.0 / 16.0;
                    maxZ = maxY = 11.0 / 16.0;
                    maxX = 2.0 / 16.0;
                    minX = 0.0;
                    break;
                case NORTH:
                    minY = minX = 5.0 / 16.0;
                    maxY = maxX = 11.0 / 16.0;
                    maxZ = 1.0;
                    minZ = 14.0 / 16.0;
                    break;
                case SOUTH:
                    minY = minX = 5.0 / 16.0;
                    maxY = maxX = 11.0 / 16.0;
                    maxZ = 2.0 / 16.0;
                    minZ = 0.0;
                    break;
                case UP:
                    minZ = minX = 5.0 / 16.0;
                    maxZ = maxX = 11.0 / 16.0;
                    maxY = 2.0 / 16.0;
                    minY = 0.0;
                    break;
                case WEST:
                    minZ = minY = 5.0 / 16.0;
                    maxZ = maxY = 11.0 / 16.0;
                    maxX = 1.0;
                    minX = 14.0 / 16.0;
                    break;
                default:
                    break;
            }

            return Shapes.create(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
        }
        return Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos,
                                        CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

}
