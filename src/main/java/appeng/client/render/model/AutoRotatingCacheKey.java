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

package appeng.client.render.model;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;

/**
 * Used as the cache key for caching automatically rotated baked models.
 */
final class AutoRotatingCacheKey {
    private final BlockState blockState;
    private final AEModelData modelData;
    private final Direction side;

    AutoRotatingCacheKey(BlockState blockState, AEModelData modelData, Direction side) {
        this.blockState = blockState;
        this.modelData = modelData;
        this.side = side;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public AEModelData getModelData() {
        return modelData;
    }

    public Direction getSide() {
        return this.side;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        AutoRotatingCacheKey cacheKey = (AutoRotatingCacheKey) o;
        return this.blockState.equals(cacheKey.blockState) && this.modelData.equals(cacheKey.modelData)
                && this.side == cacheKey.side;
    }

    @Override
    public int hashCode() {
        int result = this.blockState.hashCode();
        result = 31 * result + this.modelData.hashCode();
        result = 31 * result + (this.side != null ? this.side.hashCode() : 0);
        return result;
    }
}
