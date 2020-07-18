/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.block.qnb;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.tile.qnb.QuantumBridgeBlockEntity;

public abstract class QuantumBaseBlock extends AEBaseTileBlock<QuantumBridgeBlockEntity> {

    public static final BooleanProperty FORMED = BooleanProperty.of("formed");

    private static final VoxelShape SHAPE;

    static {
        final float shave = 2.0f / 16.0f;
        SHAPE = VoxelShapes.cuboid(new Box(shave, shave, shave, 1.0f - shave, 1.0f - shave, 1.0f - shave));
    }

    public QuantumBaseBlock(Settings props) {
        super(props);
        this.setDefaultState(this.getDefaultState().with(FORMED, false));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FORMED);
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, QuantumBridgeBlockEntity te) {
        return currentState.with(FORMED, te.isFormed());
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        final QuantumBridgeBlockEntity bridge = this.getBlockEntity(world, pos);
        if (bridge != null) {
            bridge.neighborUpdate(fromPos);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World w, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == state.getBlock()) {
            return; // Just a block state change
        }

        final QuantumBridgeBlockEntity bridge = this.getBlockEntity(w, pos);
        if (bridge != null) {
            bridge.breakCluster();
        }

        super.onStateReplaced(state, w, pos, newState, isMoving);
    }

}
