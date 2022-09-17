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
import net.minecraft.init.Blocks;


public class Fallout {
    private final MeteoriteBlockPutter putter;
    private final IBlockDefinition skyStoneDefinition;

    public Fallout(final MeteoriteBlockPutter putter, final IBlockDefinition skyStoneDefinition) {
        this.putter = putter;
        this.skyStoneDefinition = skyStoneDefinition;
    }

    public int adjustCrater() {
        return 0;
    }

    public void getRandomFall(final IMeteoriteWorld w, final int x, final int y, final int z) {
        final double a = Math.random();
        if (a > 0.9) {
            this.putter.put(w, x, y, z, Blocks.STONE);
        } else if (a > 0.8) {
            this.putter.put(w, x, y, z, Blocks.COBBLESTONE);
        } else if (a > 0.7) {
            this.putter.put(w, x, y, z, Blocks.DIRT);
        } else {
            this.putter.put(w, x, y, z, Blocks.GRAVEL);
        }
    }

    public void getRandomInset(final IMeteoriteWorld w, final int x, final int y, final int z) {
        final double a = Math.random();
        if (a > 0.9) {
            this.putter.put(w, x, y, z, Blocks.COBBLESTONE);
        } else if (a > 0.8) {
            this.putter.put(w, x, y, z, Blocks.STONE);
        } else if (a > 0.7) {
            this.putter.put(w, x, y, z, Blocks.GRASS);
        } else if (a > 0.6) {
            this.skyStoneDefinition.maybeBlock().ifPresent(block -> this.putter.put(w, x, y, z, block));
        } else if (a > 0.5) {
            this.putter.put(w, x, y, z, Blocks.GRAVEL);
        } else {
            this.putter.put(w, x, y, z, Platform.AIR_BLOCK);
        }
    }
}
