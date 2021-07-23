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

package appeng.decorative.solid;

import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.core.Direction;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;

public class QuartzGlassBlock extends AbstractGlassBlock {

    public QuartzGlassBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    public boolean skipRendering(BlockState state, net.minecraft.world.level.block.state.BlockState adjacentBlockState, net.minecraft.core.Direction side) {
        if (adjacentBlockState.getBlock() instanceof QuartzGlassBlock
                && adjacentBlockState.getRenderShape() == state.getRenderShape()) {
            return true;
        }

        return super.skipRendering(state, adjacentBlockState, side);
    }

}
