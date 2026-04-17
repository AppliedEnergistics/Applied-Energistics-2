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

package appeng.client.render;

import appeng.api.util.AEColor;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Returns the shades of a single AE color for tint indices 0, 1, and 2.
 */
public class StaticBlockColor implements BlockTintSource {
    private final AEColor color;
    private final int tintIndex;

    public StaticBlockColor(AEColor color, int tintIndex) {
        this.color = color;
        this.tintIndex = tintIndex;
    }

    @Override
    public int color(BlockState state) {
        return this.color.getVariantByTintIndex(tintIndex);
    }
}
