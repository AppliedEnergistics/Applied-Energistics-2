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

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;

import appeng.client.render.BasicUnbakedModel;

/**
 * The parent model for the compass baked model. Declares the dependencies for the base and pointer submodels mostly.
 */
public class MeteoriteCompassModel implements BasicUnbakedModel {

    private static final ResourceLocation MODEL_BASE = ResourceLocation.parse(
            "ae2:item/meteorite_compass_base");

    private static final ResourceLocation MODEL_POINTER = ResourceLocation.parse(
            "ae2:item/meteorite_compass_pointer");

    @Override
    public BakedModel bake(TextureSlots textures, ModelBaker baker, ModelState modelState, boolean useAmbientOcclusion,
            boolean usesBlockLight, ItemTransforms itemTransforms, ContextMap additionalProperties) {
        BakedModel baseModel = baker.bake(MODEL_BASE, modelState);
        BakedModel pointerModel = baker.bake(MODEL_POINTER, modelState);
        return new MeteoriteCompassBakedModel(baseModel, pointerModel);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableSet.of(MODEL_BASE, MODEL_POINTER);
    }

}
