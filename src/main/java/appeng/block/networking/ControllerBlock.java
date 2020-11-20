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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import appeng.block.AEBaseTileBlock;
import appeng.tile.networking.ControllerBlockEntity;

public class ControllerBlock extends AEBaseTileBlock<ControllerBlockEntity> {

    public enum ControllerBlockState implements StringIdentifiable {
        offline, online, conflicted;

        @Override
        public String asString() {
            return this.name();
        }

    }

    /**
     * Controls the rendering of the controller block (connected texture style). inside_a and inside_b are alternating
     * patterns for a controller that is enclosed by other controllers, and since they are always offline, they do not
     * have the usual sub-states.
     */
    public enum ControllerRenderType implements StringIdentifiable {
        block, column_x, column_y, column_z, inside_a, inside_b;

        @Override
        public String asString() {
            return this.name();
        }

    }

    public static final EnumProperty<ControllerBlockState> CONTROLLER_STATE = EnumProperty.of("state",
            ControllerBlockState.class);

    public static final EnumProperty<ControllerRenderType> CONTROLLER_TYPE = EnumProperty.of("type",
            ControllerRenderType.class);

    public ControllerBlock() {
        super(defaultProps(Material.METAL).strength(6));
        this.setDefaultState(this.getDefaultState().with(CONTROLLER_STATE, ControllerBlockState.offline)
                .with(CONTROLLER_TYPE, ControllerRenderType.block));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(CONTROLLER_STATE);
        builder.add(CONTROLLER_TYPE);
    }

    /**
     * This will compute the AE_BLOCK_FORWARD, AE_BLOCK_UP and CONTROLLER_TYPE block states based on adjacent
     * controllers and the network state of this controller (offline, online, conflicted). This is used to get a
     * rudimentary connected texture feel for the controller based on how it is placed.
     */
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState facingState,
            WorldAccess world, BlockPos pos, BlockPos facingPos) {

        // FIXME: this might work, or might _NOT_ work, but needs to be investigated

        // Only used for columns, really
        ControllerRenderType type = ControllerRenderType.block;

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        // Detect whether controllers are on both sides of the x, y, and z axes
        final boolean xx = this.getBlockEntity(world, x - 1, y, z) != null
                && this.getBlockEntity(world, x + 1, y, z) != null;
        final boolean yy = this.getBlockEntity(world, x, y - 1, z) != null
                && this.getBlockEntity(world, x, y + 1, z) != null;
        final boolean zz = this.getBlockEntity(world, x, y, z - 1) != null
                && this.getBlockEntity(world, x, y, z + 1) != null;

        if (xx && !yy && !zz) {
            type = ControllerRenderType.column_x;
        } else if (!xx && yy && !zz) {
            type = ControllerRenderType.column_y;
        } else if (!xx && !yy && zz) {
            type = ControllerRenderType.column_z;
        } else if ((xx ? 1 : 0) + (yy ? 1 : 0) + (zz ? 1 : 0) >= 2) {
            final int v = (Math.abs(x) + Math.abs(y) + Math.abs(z)) % 2;

            // While i'd like this to be based on the blockstate randomization feature, this
            // generates
            // an alternating pattern based on world position, so this is not 100% doable
            // with blockstates.
            if (v == 0) {
                type = ControllerRenderType.inside_a;
            } else {
                type = ControllerRenderType.inside_b;
            }
        }

        return state.with(CONTROLLER_TYPE, type);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        final ControllerBlockEntity tc = this.getBlockEntity(world, pos);
        if (tc != null) {
            tc.onNeighborChange(false);
        }
    }
}
