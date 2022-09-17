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

package appeng.worldgen.meteorite;


import appeng.api.definitions.IBlockDefinition;
import appeng.util.Platform;
import net.minecraft.block.state.IBlockState;


public class FalloutCopy extends Fallout {
    private static final double SPECIFIED_BLOCK_THRESHOLD = 0.9;
    private static final double AIR_BLOCK_THRESHOLD = 0.8;
    private static final double BLOCK_THRESHOLD_STEP = 0.1;

    private final IBlockState block;
    private final MeteoriteBlockPutter putter;

    public FalloutCopy(final IMeteoriteWorld w, final int x, final int y, final int z, final MeteoriteBlockPutter putter, final IBlockDefinition skyStoneDefinition) {
        super(putter, skyStoneDefinition);
        this.putter = putter;
        this.block = w.getBlockState(x, y, z);
    }

    @Override
    public void getRandomFall(final IMeteoriteWorld w, final int x, final int y, final int z) {
        final double a = Math.random();
        if (a > SPECIFIED_BLOCK_THRESHOLD) {
            this.putter.put(w, x, y, z, this.block, 3);
        } else {
            this.getOther(w, x, y, z, a);
        }
    }

    public void getOther(final IMeteoriteWorld w, final int x, final int y, final int z, final double a) {

    }

    @Override
    public void getRandomInset(final IMeteoriteWorld w, final int x, final int y, final int z) {
        final double a = Math.random();
        if (a > SPECIFIED_BLOCK_THRESHOLD) {
            this.putter.put(w, x, y, z, this.block, 3);
        } else if (a > AIR_BLOCK_THRESHOLD) {
            this.putter.put(w, x, y, z, Platform.AIR_BLOCK);
        } else {
            this.getOther(w, x, y, z, a - BLOCK_THRESHOLD_STEP);
        }
    }
}