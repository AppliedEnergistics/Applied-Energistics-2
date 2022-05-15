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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import appeng.worldgen.meteorite.MeteoriteBlockPutter;

public class Fallout {
    private final MeteoriteBlockPutter putter;
    private final BlockState skyStone;
    protected final RandomSource random;

    public Fallout(MeteoriteBlockPutter putter, BlockState skyStone, RandomSource random) {
        this.putter = putter;
        this.skyStone = skyStone;
        this.random = random;
    }

    public int adjustCrater() {
        return 0;
    }

    public void getRandomFall(LevelAccessor level, BlockPos pos) {
        var a = random.nextFloat();
        if (a > 0.9f) {
            this.putter.put(level, pos, Blocks.STONE.defaultBlockState());
        } else if (a > 0.8f) {
            this.putter.put(level, pos, Blocks.COBBLESTONE.defaultBlockState());
        } else if (a > 0.7f) {
            this.putter.put(level, pos, Blocks.DIRT.defaultBlockState());
        } else {
            this.putter.put(level, pos, Blocks.GRAVEL.defaultBlockState());
        }
    }

    public void getRandomInset(LevelAccessor level, BlockPos pos) {
        var a = random.nextFloat();
        if (a > 0.9f) {
            this.putter.put(level, pos, Blocks.COBBLESTONE.defaultBlockState());
        } else if (a > 0.8f) {
            this.putter.put(level, pos, Blocks.STONE.defaultBlockState());
        } else if (a > 0.7f) {
            this.putter.put(level, pos, Blocks.GRASS_BLOCK.defaultBlockState());
        } else if (a > 0.6f) {
            this.putter.put(level, pos, this.skyStone);
        } else if (a > 0.5f) {
            this.putter.put(level, pos, Blocks.GRAVEL.defaultBlockState());
        } else {
            this.putter.put(level, pos, Blocks.AIR.defaultBlockState());
        }
    }
}
