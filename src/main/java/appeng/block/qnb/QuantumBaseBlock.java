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

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

import appeng.block.AEBaseTileBlock;
import appeng.tile.qnb.QuantumBridgeTileEntity;
import net.minecraft.world.phys.shapes.CollisionContext;

public abstract class QuantumBaseBlock extends AEBaseTileBlock<QuantumBridgeTileEntity> {

    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    private static final VoxelShape SHAPE;

    static {
        final float shave = 2.0f / 16.0f;
        SHAPE = Shapes.create(new AABB(shave, shave, shave, 1.0f - shave, 1.0f - shave, 1.0f - shave));
    }

    public QuantumBaseBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.defaultBlockState().setValue(FORMED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FORMED);
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, QuantumBridgeTileEntity te) {
        return currentState.setValue(FORMED, te.isFormed());
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos,
                                boolean isMoving) {
        final QuantumBridgeTileEntity bridge = this.getTileEntity(world, pos);
        if (bridge != null) {
            bridge.neighborUpdate(fromPos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level w, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == state.getBlock()) {
            return; // Just a block state change
        }

        final QuantumBridgeTileEntity bridge = this.getTileEntity(w, pos);
        if (bridge != null) {
            bridge.breakCluster();
        }

        super.onRemove(state, w, pos, newState, isMoving);
    }

}
