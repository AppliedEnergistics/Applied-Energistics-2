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

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.misc.LightDetectorBlockEntity;
import appeng.helpers.AEMaterials;
import appeng.helpers.MetaRotation;
import appeng.hooks.INeighborChangeSensitive;

public class LightDetectorBlock extends AEBaseEntityBlock<LightDetectorBlockEntity>
        implements IOrientableBlock, INeighborChangeSensitive {

    // Used to alternate between two variants of the fixture on adjacent blocks
    public static final BooleanProperty ODD = BooleanProperty.create("odd");

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public LightDetectorBlock() {
        super(defaultProps(AEMaterials.FIXTURE).noCollission().noOcclusion());

        this.registerDefaultState(
                this.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP).setValue(ODD, false)
                        .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.FACING);
        builder.add(ODD);
        builder.add(WATERLOGGED);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        if (level instanceof Level && this.getBlockEntity(level, pos).isReady()) {
            // FIXME: This is ... uhm... fishy
            return ((Level) level).getMaxLocalRawBrightness(pos) - 6;
        }

        return 0;
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        final LightDetectorBlockEntity tld = this.getBlockEntity(level, pos);
        if (tld != null) {
            tld.updateLight();
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random rand) {
        // cancel out lightning
    }

    @Override
    public boolean isValidOrientation(LevelAccessor level, BlockPos pos, Direction forward,
            Direction up) {
        return this.canPlaceAt(level, pos, up.getOpposite());
    }

    private boolean canPlaceAt(LevelReader level, BlockPos pos, Direction dir) {
        return canSupportCenter(level, pos.relative(dir), dir.getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        // FIXME: We should / rather MUST use state here because at startup, this gets
        // called without a level

        final Direction up = this.getOrientable(level, pos).getUp();
        final double xOff = -0.3 * up.getStepX();
        final double yOff = -0.3 * up.getStepY();
        final double zOff = -0.3 * up.getStepZ();
        return Shapes
                .create(new AABB(xOff + 0.3, yOff + 0.3, zOff + 0.3, xOff + 0.7, yOff + 0.7, zOff + 0.7));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        final Direction up = this.getOrientable(level, pos).getUp();
        if (!this.canPlaceAt(level, pos, up.getOpposite())) {
            this.dropTorch(level, pos);
        }
    }

    private void dropTorch(Level level, BlockPos pos) {
        final BlockState prev = level.getBlockState(pos);
        level.destroyBlock(pos, true);
        level.sendBlockUpdated(pos, prev, level.getBlockState(pos), 3);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (this.canPlaceAt(level, pos, dir)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IOrientable getOrientable(BlockGetter level, BlockPos pos) {
        return new MetaRotation(level, pos, BlockStateProperties.FACING);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = context.getLevel().getFluidState(pos);
        BlockState blockState = this.defaultBlockState()
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);

        return blockState;
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED).booleanValue()
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(blockState);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction facing, BlockState facingState, LevelAccessor level,
            BlockPos currentPos, BlockPos facingPos) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(blockState, facing, facingState, level, currentPos, facingPos);
    }
}
