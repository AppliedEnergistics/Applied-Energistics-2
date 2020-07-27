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

package appeng.client.render;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.IModelTransform;
import net.minecraft.client.render.model.IUnbakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import appeng.core.AppEng;

/**
 * The model class for facades. Since facades wrap existing models, they don't
 * declare any dependencies here other than the cable anchor.
 */
public class DummyFluidItemModel implements BasicUnbakedModel {
    // We use this to get the default item transforms and make our lives easier
    private static final Identifier MODEL_BASE = new Identifier(AppEng.MOD_ID, "item/dummy_fluid_item_base");

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelLoader bakery,
            Function<SpriteIdentifier, Sprite> spriteGetter, IModelTransform modelTransform,
            ModelOverrideList overrides, Identifier modelLocation) {
        BakedModel bakedBaseModel = bakery.getBakedModel(MODEL_BASE, modelTransform, spriteGetter);

        return new DummyFluidDispatcherBakedModel(bakedBaseModel, spriteGetter);
    }

    @Override
    public Collection<SpriteIdentifier> getTextures(IModelConfiguration owner,
            Function<Identifier, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Collections.emptyList();
    }

}
