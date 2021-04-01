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

package appeng.worldgen.meteorite.fallout;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import appeng.worldgen.meteorite.MeteoriteBlockPutter;

public class FalloutSand extends FalloutCopy {
    private static final double GLASS_THRESHOLD = 0.66;
    private final MeteoriteBlockPutter putter;

    public FalloutSand(final IWorld w, BlockPos pos, final MeteoriteBlockPutter putter,
            final BlockState skyStone) {
        super(w, pos, putter, skyStone);
        this.putter = putter;
    }

    @Override
    public int adjustCrater() {
        return 2;
    }

    @Override
    public void getOther(final IWorld w, BlockPos pos, final double a) {
        if (a > GLASS_THRESHOLD) {
            this.putter.put(w, pos, Blocks.GLASS.getDefaultState());
        }
    }
}