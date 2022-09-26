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

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

/**
 * This implementation of IModelData allows us to know precisely which data is part of the model data. This is relevant
 * for {@link AutoRotatingBakedModel} and {@link AutoRotatingCacheKey}.
 */
public class AEModelData implements IModelData {

    public static final ModelProperty<AEModelData> AEMODEL = new ModelProperty<>();
    public static final ModelProperty<Direction> UP = new ModelProperty<>();
    public static final ModelProperty<Direction> FORWARD = new ModelProperty<>();
    public static final ModelProperty<Boolean> CACHEABLE = new ModelProperty<>();
    public static final ModelProperty<Byte> SPIN = new ModelProperty<>();

    private final Direction up;
    private final Direction forward;

    public AEModelData(Direction up, Direction forward) {
        this.up = Objects.requireNonNull(up);
        this.forward = Objects.requireNonNull(forward);
    }

    protected boolean isCacheable() {
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

    @Override
    public boolean hasProperty(ModelProperty<?> prop) {
        return prop == AEMODEL || prop == UP || prop == FORWARD || prop == CACHEABLE;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getData(ModelProperty<T> prop) {
        if (prop == AEMODEL) {
            return (T) this;
        }
        if (prop == UP) {
            return (T) this.up;
        }
        if (prop == FORWARD) {
            return (T) this.forward;
        }
        if (prop == CACHEABLE) {
            return (T) Boolean.valueOf(this.isCacheable());
        }

        return null;
    }

    @Nullable
    @Override
    public <T> T setData(ModelProperty<T> prop, T data) {
        return null;
    }
}
