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

import java.util.Collection;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import appeng.client.render.BasicUnbakedModel;

/**
 * The parent model for the compass baked model. Declares the dependencies for the base and pointer submodels mostly.
 */
public class SkyCompassModel implements BasicUnbakedModel {

    private static final ResourceLocation MODEL_BASE = new ResourceLocation(
            "appliedenergistics2:block/sky_compass_base");

    private static final ResourceLocation MODEL_POINTER = new ResourceLocation(
            "appliedenergistics2:block/sky_compass_pointer");

    @Nullable
    @Override
    public IBakedModel bakeModel(ModelBakery loader, Function<RenderMaterial, TextureAtlasSprite> textureGetter,
            IModelTransform rotationContainer, ResourceLocation modelId) {
        IBakedModel baseModel = loader.bake(MODEL_BASE, rotationContainer);
        IBakedModel pointerModel = loader.bake(MODEL_POINTER, rotationContainer);
        return new SkyCompassBakedModel(baseModel, pointerModel);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableSet.of(MODEL_BASE, MODEL_POINTER);
    }

}
