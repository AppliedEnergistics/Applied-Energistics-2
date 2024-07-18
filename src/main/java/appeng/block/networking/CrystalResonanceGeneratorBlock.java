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

package appeng.block.networking;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
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

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.api.orientation.RelativeSide;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.networking.CrystalResonanceGeneratorBlockEntity;

public class CrystalResonanceGeneratorBlock extends AEBaseEntityBlock<CrystalResonanceGeneratorBlockEntity>
        implements SimpleWaterloggedBlock {

    public enum State implements StringRepresentable {
        OFF, ON, HAS_CHANNEL;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public CrystalResonanceGeneratorBlock() {
        super(glassProps().noOcclusion().forceSolidOn());
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getVoxelShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getVoxelShape(state);
    }

    @NotNull
    private VoxelShape getVoxelShape(BlockState state) {
        var orientation = getOrientation(state);
        var forward = orientation.getSide(RelativeSide.FRONT);

        double minX = 0;
        double minY = 0;
        double minZ = 0;
        double maxX = 1;
        double maxY = 1;
        double maxZ = 1;

        switch (forward) {
            case DOWN -> {
                minZ = minX = 2.0 / 16.0;
                maxZ = maxX = 14.0 / 16.0;
                maxY = 1.0;
                minY = 1.0 / 16.0;
            }
            case EAST -> {
                minZ = minY = 2.0 / 16.0;
                maxZ = maxY = 14.0 / 16.0;
                maxX = 15.0 / 16.0;
                minX = 0.0;
            }
            case NORTH -> {
                minY = minX = 2.0 / 16.0;
                maxY = maxX = 14.0 / 16.0;
                maxZ = 1.0;
                minZ = 1.0 / 16.0;
            }
            case SOUTH -> {
                minY = minX = 2.0 / 16.0;
                maxY = maxX = 14.0 / 16.0;
                maxZ = 15.0 / 16.0;
                minZ = 0.0;
            }
            case UP -> {
                minZ = minX = 2.0 / 16.0;
                maxZ = maxX = 14.0 / 16.0;
                maxY = 15.0 / 16.0;
                minY = 0.0;
            }
            case WEST -> {
                minZ = minY = 2.0 / 16.0;
                maxZ = maxY = 14.0 / 16.0;
                maxX = 1.0;
                minX = 1.0 / 16.0;
            }
            default -> {
            }
        }

        return Shapes.create(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.facing();
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return super.getStateForPlacement(context)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
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
