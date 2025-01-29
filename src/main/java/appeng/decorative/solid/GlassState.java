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

import java.util.Arrays;
import java.util.Objects;

import net.minecraft.core.Direction;

/**
 * Immutable (and thus thread-safe) class that encapsulates the rendering state required for a connected texture glass
 * block.
 */
public final class GlassState {
    public static final GlassState DEFAULT;
    static {
        var masks = new int[6];
        Arrays.fill(masks, 0b1111); // Render all 4 borders
        var adjacentGlassBlocks = new boolean[6]; // Not adjacent to any glass block
        DEFAULT = new GlassState(masks, adjacentGlassBlocks);
    }

    private final int[] masks;
    private final boolean[] adjacentGlassBlocks;

    public GlassState(int[] masks, boolean[] adjacentGlassBlocks) {
        this.masks = masks.clone();
        this.adjacentGlassBlocks = adjacentGlassBlocks.clone();
    }

    public int getMask(Direction side) {
        return masks[side.get3DDataValue()];
    }

    public boolean hasAdjacentGlassBlock(Direction side) {
        return adjacentGlassBlocks[side.get3DDataValue()];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GlassState that))
            return false;
        return Arrays.equals(masks, that.masks) && Arrays.equals(adjacentGlassBlocks, that.adjacentGlassBlocks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(masks), Arrays.hashCode(adjacentGlassBlocks));
    }
}
