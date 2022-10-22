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

package appeng.client.render;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

/**
 * An unbaked model that has standard models as a dependency and produces a custom baked model as a result.
 */
public interface BasicUnbakedModel extends UnbakedModel {
    @Override
    default Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    default void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
        for (ResourceLocation dependency : getDependencies()) {
            function.apply(dependency).resolveParents(function);
        }
    }
}
