/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2020, AlgorithmX2, All rights reserved.
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

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * IMPORTANT: DO NOT CHANGE THE ORDER. Only append. No removals.
 */
public enum CraterType {

    /**
     * No crater at all.
     */
    NONE(null),

    /**
     * Just the default. Nothing
     */
    NORMAL(Blocks.AIR),

    /**
     * A crater lake filled with lava.
     */
    LAVA(Blocks.LAVA),

    /**
     * A lava crater lake cooled down to obsidian.
     */
    OBSIDIAN(Blocks.OBSIDIAN),

    /**
     * A crater filled with water by rain
     */
    WATER(Blocks.WATER),

    /**
     * A crater filled with snow by snowing.
     */
    SNOW(Blocks.SNOW_BLOCK),

    /**
     * A frozen water filled crater.
     */
    ICE(Blocks.ICE);

    private final Block filler;

    CraterType(Block filler) {
        this.filler = filler;
    }

    public Block getFiller() {
        return filler;
    }

}
