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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.decorative.AEDecorativeBlock;
import appeng.helpers.MetaRotation;

public class QuartzPillarBlock extends AEDecorativeBlock implements IOrientableBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    public QuartzPillarBlock(Properties props) {
        super(props);

        // The upwards facing pillar is the default (i.e. for the item model)
        this.setDefaultState(this.getDefaultState().with(AXIS, Direction.Axis.field_11052));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(AXIS);
    }

    @Override
    public IOrientable getOrientable(final IBlockReader w, final BlockPos pos) {
        return new MetaRotation(w, pos, null);
    }

}
