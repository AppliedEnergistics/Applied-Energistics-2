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
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import appeng.client.render.cablebus.FacadeBuilder;
import appeng.core.AppEng;

/**
 * The model class for facades. Since facades wrap existing models, they don't declare any dependencies here other than
 * the cable anchor.
 */
public class FacadeItemModel implements BasicUnbakedModel {

    // We use this to get the default item transforms and make our lives easier
    private static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "item/facade_base");

    @Nullable
    @Override
    public IBakedModel bakeModel(ModelBakery loader, Function<RenderMaterial, TextureAtlasSprite> textureGetter,
            IModelTransform rotationContainer, ResourceLocation modelId) {
        IBakedModel bakedBaseModel = loader.bake(MODEL_BASE, rotationContainer);
        FacadeBuilder facadeBuilder = new FacadeBuilder(loader);

        return new FacadeBakedItemModel(bakedBaseModel, facadeBuilder);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.singleton(MODEL_BASE);
    }
}
