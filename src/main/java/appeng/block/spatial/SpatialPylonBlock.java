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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import appeng.block.AEBaseEntityBlock;
import appeng.helpers.AEMaterials;
import appeng.blockentity.spatial.SpatialPylonBlockEntity;

public class SpatialPylonBlock extends AEBaseEntityBlock<SpatialPylonBlockEntity> {

    public SpatialPylonBlock() {
        super(defaultProps(AEMaterials.GLASS));
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

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        final SpatialPylonBlockEntity tsp = this.getBlockEntity(level, pos);
        if (tsp != null) {
            return tsp.getLightValue();
        }
        return super.getLightEmission(state, level, pos);
    }

}
