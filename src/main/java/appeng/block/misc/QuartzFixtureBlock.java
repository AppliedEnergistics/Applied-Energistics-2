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

import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseBlock;
import appeng.client.render.effects.ParticleTypes;
import appeng.core.AEConfig;
import appeng.core.AppEngClient;
import appeng.helpers.AEMaterials;
import appeng.helpers.MetaRotation;

public class QuartzFixtureBlock extends AEBaseBlock implements IOrientableBlock, SimpleWaterloggedBlock {

    // Cache VoxelShapes for each facing
    private static final Map<Direction, VoxelShape> SHAPES;

    static {
        SHAPES = new EnumMap<>(Direction.class);

        for (Direction facing : Direction.values()) {
            final double xOff = -0.3 * facing.getStepX();
            final double yOff = -0.3 * facing.getStepY();
            final double zOff = -0.3 * facing.getStepZ();
            VoxelShape shape = Shapes
                    .create(new AABB(xOff + 0.3, yOff + 0.3, zOff + 0.3, xOff + 0.7, yOff + 0.7, zOff + 0.7));
            SHAPES.put(facing, shape);
        }
    }

    // Cannot use the vanilla FACING property here because it excludes facing DOWN
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    // Used to alternate between two variants of the fixture on adjacent blocks
    public static final BooleanProperty ODD = BooleanProperty.create("odd");

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public QuartzFixtureBlock() {
        super(defaultProps(
                AEMaterials.FIXTURE).noCollission().noOcclusion().strength(0)
                        .lightLevel(b -> 14).sound(SoundType.GLASS));

        this.registerDefaultState(
                defaultBlockState().setValue(FACING, Direction.UP).setValue(ODD, false).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING, ODD, WATERLOGGED);
    }

    // For reference, see WallTorchBlock
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = super.getStateForPlacement(context);
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = context.getLevel().getFluidState(pos);

        // Set the even/odd property
        boolean oddPlacement = (pos.getX() + pos.getY() + pos.getZ()) % 2 != 0;
        blockstate = blockstate.setValue(ODD, oddPlacement).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);

        LevelReader levelReader = context.getLevel();
        Direction[] adirection = context.getNearestLookingDirections();

        for (Direction direction : adirection) {
            if (canPlaceAt(levelReader, pos, direction)) {
                return blockstate.setValue(FACING, direction.getOpposite());
            }
        }

        return null;
    }

    // Break the fixture if the block it is attached to is changed so that it could
    // no longer be placed
    @Override
    public BlockState updateShape(BlockState blockState, Direction facing, BlockState facingState, LevelAccessor level,
            BlockPos currentPos, BlockPos facingPos) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        Direction fixtureFacing = blockState.getValue(FACING);
        if (facing.getOpposite() == fixtureFacing && !canPlaceAt(level, currentPos, facing)) {
            return Blocks.AIR.defaultBlockState();
        }
        return blockState;
    }

    @Override
    public boolean isValidOrientation(LevelAccessor level, BlockPos pos, Direction forward,
            Direction up) {
        // FIXME: I think this entire method -> not required, but not sure... are quartz
        // fixtures rotateable???
        return this.canPlaceAt(level, pos, up.getOpposite());
    }

    private boolean canPlaceAt(LevelReader level, BlockPos pos, Direction dir) {
        final BlockPos test = pos.relative(dir);
        BlockState blockstate = level.getBlockState(test);
        return blockstate.isFaceSturdy(level, test, dir.getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return SHAPES.get(facing);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }

        if (r.nextFloat() < 0.98) {
            return;
        }

        final Direction up = this.getOrientable(level, pos).getUp();
        final double xOff = -0.3 * up.getStepX();
        final double yOff = -0.3 * up.getStepY();
        final double zOff = -0.3 * up.getStepZ();
        for (int bolts = 0; bolts < 3; bolts++) {
            if (AppEngClient.instance().shouldAddParticles(r)) {
                level.addParticle(ParticleTypes.LIGHTNING, xOff + 0.5 + pos.getX(), yOff + 0.5 + pos.getY(),
                        zOff + 0.5 + pos.getZ(), 0, 0, 0);
            }
        }
    }

    // FIXME: Replaced by the postPlaceupdate stuff above, but check item drops!
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
        return new MetaRotation(level, pos, FACING);
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED).booleanValue()
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(blockState);
    }

}
