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
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

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
            final double xOff = -0.3 * facing.getOffsetX();
            final double yOff = -0.3 * facing.getOffsetY();
            final double zOff = -0.3 * facing.getOffsetZ();
            VoxelShape shape = VoxelShapes
                    .cuboid(new Box(xOff + 0.3, yOff + 0.3, zOff + 0.3, xOff + 0.7, yOff + 0.7, zOff + 0.7));
            SHAPES.put(facing, shape);
        }
    }

    // Cannot use the vanilla FACING property here because it excludes facing DOWN
    public static final DirectionProperty FACING = Properties.FACING;

    // Used to alternate between two variants of the fixture on adjacent blocks
    public static final BooleanProperty ODD = BooleanProperty.of("odd");

    public QuartzFixtureBlock() {
        super(defaultProps(Material.DECORATION).noCollision().strength(0).lightLevel(14).sounds(BlockSoundGroup.GLASS));

        this.setDefaultState(getDefaultState().with(FACING, Direction.UP).with(ODD, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ODD);
    }

    // For reference, see WallTorchBlock
    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState blockstate = super.getPlacementState(context);
        BlockPos pos = context.getBlockPos();

        // Set the even/odd property
        boolean oddPlacement = ((pos.getX() + pos.getY() + pos.getZ()) % 2) != 0;
        blockstate = blockstate.with(ODD, oddPlacement);

        WorldView iworldreader = context.getWorld();
        Direction[] adirection = context.getPlacementDirections();

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
    public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState facingState,
            WorldAccess worldIn, BlockPos pos, BlockPos facingPos) {
        Direction fixtureFacing = state.get(FACING);
        if (facing.getOpposite() == fixtureFacing && !canPlaceAt(worldIn, pos, facing)) {
            return Blocks.AIR.getDefaultState();
        }
        return state;
    }

    @Override
    public boolean isValidOrientation(final WorldAccess w, final BlockPos pos, final Direction forward,
            final Direction up) {
        // FIXME: I think this entire method -> not required, but not sure... are quartz
        // fixtures rotateable???
        return this.canPlaceAt(w, pos, up.getOpposite());
    }

    private boolean canPlaceAt(final WorldView w, final BlockPos pos, final Direction dir) {
        final BlockPos test = pos.offset(dir);
        BlockState blockstate = w.getBlockState(test);
        return blockstate.isSideSolidFullSquare(w, test, dir.getOpposite());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        Direction facing = state.get(FACING);
        return SHAPES.get(facing);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(final BlockState state, final World w, final BlockPos pos, final Random r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }

        if (r.nextFloat() < 0.98) {
            return;
        }

        final Direction up = this.getOrientable(w, pos).getUp();
        final double xOff = -0.3 * up.getOffsetX();
        final double yOff = -0.3 * up.getOffsetY();
        final double zOff = -0.3 * up.getOffsetZ();
        for (int bolts = 0; bolts < 3; bolts++) {
            if (AppEng.instance().shouldAddParticles(r)) {
                w.addParticle(ParticleTypes.LIGHTNING, xOff + 0.5 + pos.getX(), yOff + 0.5 + pos.getY(),
                        zOff + 0.5 + pos.getZ(), 0, 0, 0);
            }
        }
    }

    // FIXME: Replaced by the postPlaceupdate stuff above, but check item drops!
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        final Direction up = this.getOrientable(world, pos).getUp();
        if (!this.canPlaceAt(world, pos, up.getOpposite())) {
            this.dropTorch(world, pos);
        }
    }

    private void dropTorch(final World w, final BlockPos pos) {
        final BlockState prev = w.getBlockState(pos);
        w.breakBlock(pos, true);
        w.updateListeners(pos, prev, w.getBlockState(pos), 3);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView w, BlockPos pos) {
        for (final Direction dir : Direction.values()) {
            if (this.canPlaceAt(w, pos, dir)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IOrientable getOrientable(final BlockView w, final BlockPos pos) {
        return new MetaRotation(w, pos, FACING);
    }

}
