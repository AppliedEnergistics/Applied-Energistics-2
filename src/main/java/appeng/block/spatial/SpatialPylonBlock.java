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

package appeng.block.spatial;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.spatial.SpatialPylonBlockEntity;
import appeng.helpers.AEMaterials;

public class SpatialPylonBlock extends AEBaseEntityBlock<SpatialPylonBlockEntity> {

    public static final BooleanProperty POWERED_ON = BooleanProperty.create("powered_on");

    public SpatialPylonBlock() {
        super(defaultProps(AEMaterials.GLASS).lightLevel(state -> {
            return state.getValue(POWERED_ON) ? 8 : 0;
        }));
        registerDefaultState(defaultBlockState().setValue(POWERED_ON, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED_ON);
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, SpatialPylonBlockEntity be) {
        var poweredOn = (be.getDisplayBits() & SpatialPylonBlockEntity.DISPLAY_POWERED_ENABLED) != 0;
        return currentState.setValue(SpatialPylonBlock.POWERED_ON, poweredOn);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        final SpatialPylonBlockEntity tsp = this.getBlockEntity(level, pos);
        if (tsp != null) {
            tsp.neighborChanged(fromPos);
        }
    }

}
