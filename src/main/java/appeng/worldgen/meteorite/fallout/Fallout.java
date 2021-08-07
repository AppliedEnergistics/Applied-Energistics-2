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

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import appeng.worldgen.meteorite.MeteoriteBlockPutter;

public class Fallout {
    private final MeteoriteBlockPutter putter;
    private final BlockState skyStone;

    public Fallout(final MeteoriteBlockPutter putter, final BlockState skyStone) {
        this.putter = putter;
        this.skyStone = skyStone;
    }

    public int adjustCrater() {
        return 0;
    }

    public void getRandomFall(final LevelAccessor level, BlockPos pos) {
        final double a = Math.random();
        if (a > 0.9) {
            this.putter.put(level, pos, Blocks.STONE.defaultBlockState());
        } else if (a > 0.8) {
            this.putter.put(level, pos, Blocks.COBBLESTONE.defaultBlockState());
        } else if (a > 0.7) {
            this.putter.put(level, pos, Blocks.DIRT.defaultBlockState());
        } else {
            this.putter.put(level, pos, Blocks.GRAVEL.defaultBlockState());
        }
    }

    public void getRandomInset(final LevelAccessor level, BlockPos pos) {
        final double a = Math.random();
        if (a > 0.9) {
            this.putter.put(level, pos, Blocks.COBBLESTONE.defaultBlockState());
        } else if (a > 0.8) {
            this.putter.put(level, pos, Blocks.STONE.defaultBlockState());
        } else if (a > 0.7) {
            this.putter.put(level, pos, Blocks.GRASS_BLOCK.defaultBlockState());
        } else if (a > 0.6) {
            this.putter.put(level, pos, this.skyStone);
        } else if (a > 0.5) {
            this.putter.put(level, pos, Blocks.GRAVEL.defaultBlockState());
        } else {
            this.putter.put(level, pos, Blocks.AIR.defaultBlockState());
        }
    }
}
