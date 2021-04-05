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

package appeng.bootstrap.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import com.google.common.collect.Sets;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.util.ResourceLocation;

import appeng.core.AppEng;

public class ModelOverrideComponent implements IModelBakeComponent {

    // Maps from resource path to customizer
    private final Map<String, BiFunction<ResourceLocation, IBakedModel, IBakedModel>> customizer = new HashMap<>();

    public void addOverride(String resourcePath, BiFunction<ResourceLocation, IBakedModel, IBakedModel> customizer) {
        this.customizer.put(resourcePath, customizer);
    }

    @Override
    public void onModelsReloaded(final Map<ResourceLocation, IBakedModel> modelRegistry) {
        Set<ResourceLocation> keys = Sets.newHashSet(modelRegistry.keySet());
        IBakedModel missingModel = modelRegistry.get(ModelBakery.MODEL_MISSING);

        for (ResourceLocation location : keys) {
            if (!location.getNamespace().equals(AppEng.MOD_ID)) {
                continue;
            }

            IBakedModel orgModel = modelRegistry.get(location);

            // Don't customize the missing model. This causes Forge to swallow exceptions
            if (orgModel == missingModel) {
                continue;
            }

            BiFunction<ResourceLocation, IBakedModel, IBakedModel> customizer = this.customizer.get(location.getPath());
            if (customizer != null) {
                IBakedModel newModel = customizer.apply(location, orgModel);

                if (newModel != orgModel) {
                    modelRegistry.put(location, newModel);
                }
            }
        }
    }
}
