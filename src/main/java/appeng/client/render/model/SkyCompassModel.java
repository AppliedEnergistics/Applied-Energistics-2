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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import appeng.client.render.BasicUnbakedModel;

/**
 * The parent model for the compass baked model. Declares the dependencies for
 * the base and pointer submodels mostly.
 */
public class SkyCompassModel implements BasicUnbakedModel {

    private static final Identifier MODEL_BASE = new Identifier("appliedenergistics2:block/sky_compass_base");

    private static final Identifier MODEL_POINTER = new Identifier("appliedenergistics2:block/sky_compass_pointer");

    public static final List<Identifier> DEPENDENCIES = ImmutableList.of(MODEL_BASE, MODEL_POINTER);

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter,
            ModelBakeSettings rotationContainer, Identifier modelId) {
        BakedModel baseModel = loader.bake(MODEL_BASE, rotationContainer);
        BakedModel pointerModel = loader.bake(MODEL_POINTER, rotationContainer);
        return new SkyCompassBakedModel(baseModel, pointerModel);
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return ImmutableSet.of(MODEL_BASE, MODEL_POINTER);
    }

}
