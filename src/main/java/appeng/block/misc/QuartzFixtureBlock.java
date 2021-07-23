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
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
    private static final Map<Direction, net.minecraft.world.phys.shapes.VoxelShape> SHAPES;

    static {
        SHAPES = new EnumMap<>(Direction.class);

        for (Direction facing : Direction.values()) {
            final double xOff = -0.3 * facing.getStepX();
            final double yOff = -0.3 * facing.getStepY();
            final double zOff = -0.3 * facing.getStepZ();
            net.minecraft.world.phys.shapes.VoxelShape shape = Shapes
                    .create(new AABB(xOff + 0.3, yOff + 0.3, zOff + 0.3, xOff + 0.7, yOff + 0.7, zOff + 0.7));
            SHAPES.put(facing, shape);
        }
    }

    // Cannot use the vanilla FACING property here because it excludes facing DOWN
    public static final DirectionProperty FACING = net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

    // Used to alternate between two variants of the fixture on adjacent blocks
    public static final BooleanProperty ODD = BooleanProperty.create("odd");

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public QuartzFixtureBlock() {
        super(defaultProps(
                AEMaterials.FIXTURE).noCollission().noOcclusion().strength(0)
                        .lightLevel(b -> 14).sound(SoundType.GLASS));

        this.registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP).setValue(ODD, false).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, ODD, WATERLOGGED);
    }

    // For reference, see WallTorchBlock
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = super.getStateForPlacement(context);
        net.minecraft.core.BlockPos pos = context.getClickedPos();
        net.minecraft.world.level.material.FluidState fluidState = context.getLevel().getFluidState(pos);

        // Set the even/odd property
        boolean oddPlacement = (pos.getX() + pos.getY() + pos.getZ()) % 2 != 0;
        blockstate = blockstate.setValue(ODD, oddPlacement).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);

        LevelReader iworldreader = context.getLevel();
        net.minecraft.core.Direction[] adirection = context.getNearestLookingDirections();

        for (Direction direction : adirection) {
            if (canPlaceAt(iworldreader, pos, direction)) {
                return blockstate.setValue(FACING, direction.getOpposite());
            }
        }

        return null;
    }

    // Break the fixture if the block it is attached to is changed so that it could
    // no longer be placed
    @Override
    public BlockState updateShape(BlockState blockState, net.minecraft.core.Direction facing, net.minecraft.world.level.block.state.BlockState facingState, LevelAccessor world,
                                  BlockPos currentPos, net.minecraft.core.BlockPos facingPos) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER,
                    Fluids.WATER.getTickDelay(world));
        }

        Direction fixtureFacing = blockState.getValue(FACING);
        if (facing.getOpposite() == fixtureFacing && !canPlaceAt(world, currentPos, facing)) {
            return Blocks.AIR.defaultBlockState();
        }
        return blockState;
    }

    @Override
    public boolean isValidOrientation(final LevelAccessor w, final net.minecraft.core.BlockPos pos, final net.minecraft.core.Direction forward, final net.minecraft.core.Direction up) {
        // FIXME: I think this entire method -> not required, but not sure... are quartz
        // fixtures rotateable???
        return this.canPlaceAt(w, pos, up.getOpposite());
    }

    private boolean canPlaceAt(final LevelReader w, final net.minecraft.core.BlockPos pos, final Direction dir) {
        final net.minecraft.core.BlockPos test = pos.relative(dir);
        net.minecraft.world.level.block.state.BlockState blockstate = w.getBlockState(test);
        return blockstate.isFaceSturdy(w, test, dir.getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, net.minecraft.core.BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return SHAPES.get(facing);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(final BlockState state, final Level w, final net.minecraft.core.BlockPos pos, final Random r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }

        if (r.nextFloat() < 0.98) {
            return;
        }

        final Direction up = this.getOrientable(w, pos).getUp();
        final double xOff = -0.3 * up.getStepX();
        final double yOff = -0.3 * up.getStepY();
        final double zOff = -0.3 * up.getStepZ();
        for (int bolts = 0; bolts < 3; bolts++) {
            if (AppEngClient.instance().shouldAddParticles(r)) {
                w.addParticle(ParticleTypes.LIGHTNING, xOff + 0.5 + pos.getX(), yOff + 0.5 + pos.getY(),
                        zOff + 0.5 + pos.getZ(), 0, 0, 0);
            }
        }
    }

    // FIXME: Replaced by the postPlaceupdate stuff above, but check item drops!
    @Override
    public void neighborChanged(net.minecraft.world.level.block.state.BlockState state, Level world, net.minecraft.core.BlockPos pos, Block blockIn, BlockPos fromPos,
                                boolean isMoving) {
        final Direction up = this.getOrientable(world, pos).getUp();
        if (!this.canPlaceAt(world, pos, up.getOpposite())) {
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
    public IOrientable getOrientable(final BlockGetter w, final BlockPos pos) {
        return new MetaRotation(w, pos, FACING);
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED).booleanValue()
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(blockState);
    }

}
