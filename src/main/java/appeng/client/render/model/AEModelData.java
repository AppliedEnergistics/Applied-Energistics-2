/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import java.util.Objects;

import net.minecraft.core.Direction;

/**
 * This implementation of IModelData allows us to know precisely which data is part of the model data. This is relevant
 * for {@link AutoRotatingBakedModel} and {@link AutoRotatingCacheKey}.
 */
public class AEModelData {

    private final Direction up;
    private final Direction forward;

    public AEModelData(Direction up, Direction forward) {
        this.up = Objects.requireNonNull(up);
        this.forward = Objects.requireNonNull(forward);
    }

    public Direction getUp() {
        return up;
    }

    public Direction getForward() {
        return forward;
    }

    public boolean isCacheable() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AEModelData that = (AEModelData) o;
        return up == that.up && forward == that.forward;
    }

    @Override
    public int hashCode() {
        return Objects.hash(up, forward);
    }

}
