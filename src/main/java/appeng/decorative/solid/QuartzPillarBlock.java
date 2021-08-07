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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.decorative.AEDecorativeBlock;
import appeng.helpers.MetaRotation;

public class QuartzPillarBlock extends AEDecorativeBlock implements IOrientableBlock {
    public static final EnumProperty<Axis> AXIS = BlockStateProperties.AXIS;

    public QuartzPillarBlock(BlockBehaviour.Properties props) {
        super(props);

        // The upwards facing pillar is the default (i.e. for the item model)
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Axis.Y));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS);
    }

    @Override
    public IOrientable getOrientable(final BlockGetter level, final BlockPos pos) {
        return new MetaRotation(level, pos, null);
    }

}
