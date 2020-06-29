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

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.IModelTransform;
import net.minecraft.client.render.model.IUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

/**
 * The parent model for the compass baked model. Declares the dependencies for
 * the base and pointer submodels mostly.
 */
public class SkyCompassModel implements IModelGeometry<SkyCompassModel> {

    private static final Identifier MODEL_BASE = new Identifier(
            "appliedenergistics2:block/sky_compass_base");

    private static final Identifier MODEL_POINTER = new Identifier(
            "appliedenergistics2:block/sky_compass_pointer");

    public static final List<Identifier> DEPENDENCIES = ImmutableList.of(MODEL_BASE, MODEL_POINTER);

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelLoader bakery,
                           Function<SpriteIdentifier, Sprite> spriteGetter, IModelTransform modelTransform,
                           ModelOverrideList overrides, Identifier modelLocation) {
        BakedModel baseModel = bakery.getBakedModel(MODEL_BASE, modelTransform, spriteGetter);
        BakedModel pointerModel = bakery.getBakedModel(MODEL_POINTER, modelTransform, spriteGetter);
        return new SkyCompassBakedModel(baseModel, pointerModel);
    }

    @Override
    public Collection<SpriteIdentifier> getTextures(IModelConfiguration owner,
                                                    Function<Identifier, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Collections.emptyList();
    }

}
