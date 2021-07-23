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
import net.minecraft.util.Mth;

import appeng.block.AEBaseBlock;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;

public class QuartzOreBlock extends AEBaseBlock {
    public QuartzOreBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    public int getExpDrop(BlockState state, LevelReader reader, BlockPos pos, int fortune,
                          int silktouch) {
        return silktouch == 0 ? Mth.nextInt(RANDOM, 2, 5) : 0;
    }

}
