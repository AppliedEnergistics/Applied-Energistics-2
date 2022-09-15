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
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

/**
 * This implementation of IModelData allows us to know precisely which data is part of the model data. This is relevant
 * for {@link AutoRotatingBakedModel} and {@link AutoRotatingCacheKey}.
 */
public final class AEModelData {

    public static final ModelProperty<Direction> UP = new ModelProperty<>();
    public static final ModelProperty<Direction> FORWARD = new ModelProperty<>();
    public static final ModelProperty<Boolean> SKIP_CACHE = new ModelProperty<>();
    public static final ModelProperty<Byte> SPIN = new ModelProperty<>();

    public static ModelData.Builder builder(Direction up, Direction forward) {
        return ModelData.builder()
                .with(UP, up)
                .with(FORWARD, forward);
    }

    public static ModelData create(Direction up, Direction forward) {
        return builder(up, forward).build();
    }

    public static byte getSpin(ModelData modelData) {
        return Objects.requireNonNullElse(modelData.get(SPIN), (byte) 0);
    }

    public static Direction getForward(ModelData modelData) {
        return Objects.requireNonNullElse(modelData.get(FORWARD), Direction.NORTH);
    }

    public static Direction getUp(ModelData modelData) {
        return Objects.requireNonNullElse(modelData.get(UP), Direction.UP);
    }
}
