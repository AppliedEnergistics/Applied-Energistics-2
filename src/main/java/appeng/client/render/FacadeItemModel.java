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

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import appeng.client.render.cablebus.FacadeBuilder;
import appeng.core.AppEng;

/**
 * The model class for facades. Since facades wrap existing models, they don't
 * declare any dependencies here other than the cable anchor.
 */
public class FacadeItemModel implements IUnbakedModel {
    // We use this to get the default item transforms and make our lives easier
    private static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "item/facade_base");

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.singleton(MODEL_BASE);
    }

    @Override
    public Collection<Material> getTextures(Function<ResourceLocation, IUnbakedModel> modelGetter,
            Set<Pair<String, String>> missingTextureErrors) {
        return Collections.emptyList();
    }

    @Override
    public IBakedModel bakeModel(ModelBakery modelBakeryIn, Function<Material, TextureAtlasSprite> spriteGetterIn,
            IModelTransform transformIn, ResourceLocation locationIn) {
        IBakedModel bakedBaseModel = modelBakeryIn.getBakedModel(MODEL_BASE, transformIn, spriteGetterIn);
        FacadeBuilder facadeBuilder = new FacadeBuilder();

        return new FacadeDispatcherBakedModel(bakedBaseModel, facadeBuilder);
    }
}
