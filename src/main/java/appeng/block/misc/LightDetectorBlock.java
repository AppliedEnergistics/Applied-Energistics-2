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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
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
import appeng.block.AEBaseTileBlock;
import appeng.helpers.MetaRotation;
import appeng.tile.misc.LightDetectorBlockEntity;

public class LightDetectorBlock extends AEBaseTileBlock<LightDetectorBlockEntity> implements IOrientableBlock {

    // Used to alternate between two variants of the fixture on adjacent blocks
    public static final BooleanProperty ODD = BooleanProperty.of("odd");

    public LightDetectorBlock() {
        super(defaultProps(Material.SUPPORTED));
        this.setDefaultState(this.getDefaultState().with(Properties.FACING, Direction.UP).with(ODD, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.FACING);
        builder.add(ODD);
    }

    @Override
    public int getWeakRedstonePower(final BlockState state, final BlockView w, final BlockPos pos,
            final Direction side) {
        if (w instanceof World && this.getBlockEntity(w, pos).isReady()) {
            // FIXME: This is ... uhm... fishy
            return ((World) w).getLightLevel(pos) - 6;
        }

        return 0;
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState,
            WorldAccess world, BlockPos pos, BlockPos posFrom) {
        final Direction up = this.getOrientable(world, pos).getUp();
        if (!this.canPlaceAt(world, pos, up.getOpposite())) {
            // FIXME: Double check that this actually updates neighbors
            return Blocks.AIR.getDefaultState();
        }

        final LightDetectorBlockEntity tld = this.getBlockEntity(world, pos);
        if (tld != null) {
            tld.updateLight();
        }

        return state;
    }

    @Override
    public boolean isValidOrientation(final WorldAccess w, final BlockPos pos, final Direction forward,
            final Direction up) {
        return this.canPlaceAt(w, pos, up.getOpposite());
    }

    private boolean canPlaceAt(final BlockView w, final BlockPos pos, final Direction dir) {
        final BlockPos test = pos.offset(dir);
        BlockState blockstate = w.getBlockState(test);
        return blockstate.isSideSolidFullSquare(w, test, dir.getOpposite());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView w, BlockPos pos, ShapeContext context) {

        // FIXME: We should / rather MUST use state here because at startup, this gets
        // called without a world

        final Direction up = this.getOrientable(w, pos).getUp();
        final double xOff = -0.3 * up.getOffsetX();
        final double yOff = -0.3 * up.getOffsetY();
        final double zOff = -0.3 * up.getOffsetZ();
        return VoxelShapes.cuboid(new Box(xOff + 0.3, yOff + 0.3, zOff + 0.3, xOff + 0.7, yOff + 0.7, zOff + 0.7));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
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
        return new MetaRotation(w, pos, Properties.FACING);
    }

}
