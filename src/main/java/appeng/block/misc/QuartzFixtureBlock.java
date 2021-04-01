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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseBlock;
import appeng.client.render.effects.ParticleTypes;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.helpers.MetaRotation;

public class QuartzFixtureBlock extends AEBaseBlock implements IOrientableBlock {

    // Cache VoxelShapes for each facing
    private static final Map<Direction, VoxelShape> SHAPES;

    static {
        SHAPES = new EnumMap<>(Direction.class);

        for (Direction facing : Direction.values()) {
            final double xOff = -0.3 * facing.getXOffset();
            final double yOff = -0.3 * facing.getYOffset();
            final double zOff = -0.3 * facing.getZOffset();
            VoxelShape shape = VoxelShapes
                    .create(new AxisAlignedBB(xOff + 0.3, yOff + 0.3, zOff + 0.3, xOff + 0.7, yOff + 0.7, zOff + 0.7));
            SHAPES.put(facing, shape);
        }
    }

    // Cannot use the vanilla FACING property here because it excludes facing DOWN
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    // Used to alternate between two variants of the fixture on adjacent blocks
    public static final BooleanProperty ODD = BooleanProperty.create("odd");

    public QuartzFixtureBlock() {
        super(defaultProps(Material.MISCELLANEOUS).doesNotBlockMovement().hardnessAndResistance(0)
                .setLightLevel((b) -> 14).sound(SoundType.GLASS));

        this.setDefaultState(getDefaultState().with(FACING, Direction.UP).with(ODD, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, ODD);
    }

    // For reference, see WallTorchBlock
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockstate = super.getStateForPlacement(context);
        BlockPos pos = context.getPos();

        // Set the even/odd property
        boolean oddPlacement = ((pos.getX() + pos.getY() + pos.getZ()) % 2) != 0;
        blockstate = blockstate.with(ODD, oddPlacement);

        IWorldReader iworldreader = context.getWorld();
        Direction[] adirection = context.getNearestLookingDirections();

        for (Direction direction : adirection) {
            if (canPlaceAt(iworldreader, pos, direction)) {
                return blockstate.with(FACING, direction.getOpposite());
            }
        }

        return null;
    }

    // Break the fixture if the block it is attached to is changed so that it could
    // no longer be placed
    @Override
    public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState,
            IWorld worldIn, BlockPos pos, BlockPos facingPos) {
        Direction fixtureFacing = state.get(FACING);
        if (facing.getOpposite() == fixtureFacing && !canPlaceAt(worldIn, pos, facing)) {
            return Blocks.AIR.getDefaultState();
        }
        return state;
    }

    @Override
    public boolean isValidOrientation(final IWorld w, final BlockPos pos, final Direction forward,
            final Direction up) {
        // FIXME: I think this entire method -> not required, but not sure... are quartz
        // fixtures rotateable???
        return this.canPlaceAt(w, pos, up.getOpposite());
    }

    private boolean canPlaceAt(final IWorldReader w, final BlockPos pos, final Direction dir) {
        final BlockPos test = pos.offset(dir);
        BlockState blockstate = w.getBlockState(test);
        return blockstate.isSolidSide(w, test, dir.getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction facing = state.get(FACING);
        return SHAPES.get(facing);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void animateTick(final BlockState state, final World w, final BlockPos pos, final Random r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }

        if (r.nextFloat() < 0.98) {
            return;
        }

        final Direction up = this.getOrientable(w, pos).getUp();
        final double xOff = -0.3 * up.getXOffset();
        final double yOff = -0.3 * up.getYOffset();
        final double zOff = -0.3 * up.getZOffset();
        for (int bolts = 0; bolts < 3; bolts++) {
            if (AppEng.instance().shouldAddParticles(r)) {
                w.addParticle(ParticleTypes.LIGHTNING, xOff + 0.5 + pos.getX(), yOff + 0.5 + pos.getY(),
                        zOff + 0.5 + pos.getZ(), 0, 0, 0);
            }
        }
    }

    // FIXME: Replaced by the postPlaceupdate stuff above, but check item drops!
    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        final Direction up = this.getOrientable(world, pos).getUp();
        if (!this.canPlaceAt(world, pos, up.getOpposite())) {
            this.dropTorch(world, pos);
        }
    }

    private void dropTorch(final World w, final BlockPos pos) {
        final BlockState prev = w.getBlockState(pos);
        w.destroyBlock(pos, true);
        w.notifyBlockUpdate(pos, prev, w.getBlockState(pos), 3);
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader w, BlockPos pos) {
        for (final Direction dir : Direction.values()) {
            if (this.canPlaceAt(w, pos, dir)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IOrientable getOrientable(final IBlockReader w, final BlockPos pos) {
        return new MetaRotation(w, pos, FACING);
    }

}
