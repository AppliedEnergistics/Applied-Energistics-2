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

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;

import appeng.client.render.BasicUnbakedModel;

/**
 * The parent model for the compass baked model. Declares the dependencies for the base and pointer submodels mostly.
 */
public class MeteoriteCompassModel implements BasicUnbakedModel<MeteoriteCompassModel> {

    private static final ResourceLocation MODEL_BASE = new ResourceLocation(
            "ae2:item/meteorite_compass_base");

    private static final ResourceLocation MODEL_POINTER = new ResourceLocation(
            "ae2:item/meteorite_compass_pointer");

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform,
            ItemOverrides overrides, ResourceLocation modelLocation) {
        BakedModel baseModel = bakery.bake(MODEL_BASE, modelTransform, spriteGetter);
        BakedModel pointerModel = bakery.bake(MODEL_POINTER, modelTransform, spriteGetter);
        return new MeteoriteCompassBakedModel(baseModel, pointerModel);
    }

    @Override
    public Collection<ResourceLocation> getModelDependencies() {
        return ImmutableSet.of(MODEL_BASE, MODEL_POINTER);
    }

}
