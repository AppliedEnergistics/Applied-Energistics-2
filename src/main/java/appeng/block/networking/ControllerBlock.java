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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.networking.ControllerBlockEntity;

public class ControllerBlock extends AEBaseEntityBlock<ControllerBlockEntity> {

    public enum ControllerBlockState implements StringRepresentable {
        offline, online, conflicted;

        @Override
        public String getSerializedName() {
            return this.name();
        }

    }

    /**
     * Controls the rendering of the controller block (connected texture style). inside_a and inside_b are alternating
     * patterns for a controller that is enclosed by other controllers, and since they are always offline, they do not
     * have the usual sub-states.
     */
    public enum ControllerRenderType implements StringRepresentable {
        block, column_x, column_y, column_z, inside_a, inside_b;

        @Override
        public String getSerializedName() {
            return this.name();
        }

    }

    public static final EnumProperty<ControllerBlockState> CONTROLLER_STATE = EnumProperty.create("state",
            ControllerBlockState.class);

    public static final EnumProperty<ControllerRenderType> CONTROLLER_TYPE = EnumProperty.create("type",
            ControllerRenderType.class);

    public ControllerBlock() {
        super(defaultProps(Material.METAL).strength(6));
        this.registerDefaultState(this.defaultBlockState().setValue(CONTROLLER_STATE, ControllerBlockState.offline)
                .setValue(CONTROLLER_TYPE, ControllerRenderType.block));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CONTROLLER_STATE);
        builder.add(CONTROLLER_TYPE);
    }

    /**
     * This will compute the AE_BLOCK_FORWARD, AE_BLOCK_UP and CONTROLLER_TYPE block states based on adjacent
     * controllers and the network state of this controller (offline, online, conflicted). This is used to get a
     * rudimentary connected texture feel for the controller based on how it is placed.
     */
    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
            BlockPos pos, BlockPos facingPos) {

        // FIXME: this might work, or might _NOT_ work, but needs to be investigated

        // Only used for columns, really
        ControllerRenderType type = ControllerRenderType.block;

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        // Detect whether controllers are on both sides of the x, y, and z axes
        final boolean xx = this.getBlockEntity(level, x - 1, y, z) != null
                && this.getBlockEntity(level, x + 1, y, z) != null;
        final boolean yy = this.getBlockEntity(level, x, y - 1, z) != null
                && this.getBlockEntity(level, x, y + 1, z) != null;
        final boolean zz = this.getBlockEntity(level, x, y, z - 1) != null
                && this.getBlockEntity(level, x, y, z + 1) != null;

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
            // an alternating pattern based on level position, so this is not 100% doable
            // with blockstates.
            if (v == 0) {
                type = ControllerRenderType.inside_a;
            } else {
                type = ControllerRenderType.inside_b;
            }
        }

        return state.setValue(CONTROLLER_TYPE, type);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        final ControllerBlockEntity tc = this.getBlockEntity(level, pos);
        if (tc != null) {
            tc.updateState();
        }
    }
}
