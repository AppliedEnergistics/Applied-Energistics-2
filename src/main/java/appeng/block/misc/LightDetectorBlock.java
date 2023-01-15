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

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.misc.LightDetectorBlockEntity;
import appeng.helpers.AEMaterials;

public class LightDetectorBlock extends AEBaseEntityBlock<LightDetectorBlockEntity> {

    // Cache VoxelShapes for each facing
    private static final Map<Direction, VoxelShape> SHAPES;

    static {
        SHAPES = new EnumMap<>(Direction.class);

        for (var facing : Direction.values()) {
            var xOff = -0.3 * facing.getStepX();
            var yOff = -0.3 * facing.getStepY();
            var zOff = -0.3 * facing.getStepZ();
            VoxelShape shape = Shapes
                    .create(xOff + 0.3, yOff + 0.3, zOff + 0.3, xOff + 0.7, yOff + 0.7, zOff + 0.7);
            SHAPES.put(facing, shape);
        }
    }

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    // Used to alternate between two variants of the fixture on adjacent blocks
    public static final BooleanProperty ODD = BooleanProperty.create("odd");

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public LightDetectorBlock() {
        super(defaultProps(AEMaterials.FIXTURE).noCollission().noOcclusion().sound(SoundType.GLASS));

        this.registerDefaultState(
                this.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP).setValue(ODD, false)
                        .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.FACING, ODD, WATERLOGGED);
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
        var tld = this.getBlockEntity(level, pos);
        if (tld != null) {
            tld.updateLight();
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return Shapes.empty();
    }

    private void dropTorch(Level level, BlockPos pos) {
        final BlockState prev = level.getBlockState(pos);
        level.destroyBlock(pos, true);
        level.sendBlockUpdated(pos, prev, level.getBlockState(pos), 3);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        var facing = state.getValue(FACING);
        var blockPos = pos.relative(facing.getOpposite());
        return canSupportCenter(level, blockPos, facing);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = context.getLevel().getFluidState(pos);

        // Set the even/odd property
        boolean oddPlacement = (pos.getX() + pos.getY() + pos.getZ()) % 2 != 0;
        state = state.setValue(ODD, oddPlacement).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);

        var levelReader = context.getLevel();
        var adirection = context.getNearestLookingDirections();

        for (var direction : adirection) {
            var placedState = state.setValue(FACING, direction.getOpposite());
            if (canSurvive(placedState, levelReader, pos)) {
                return placedState;
            }
        }

        return null;
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED).booleanValue()
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(blockState);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor level,
            BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED).booleanValue()) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return state;
    }
}
