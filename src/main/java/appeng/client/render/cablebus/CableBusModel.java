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

package appeng.client.render.cablebus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import appeng.api.parts.PartModelsInternal;
import appeng.api.util.AEColor;
import appeng.client.render.BasicUnbakedModel;
import appeng.core.AELog;
import appeng.core.AppEng;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

/**
 * The built-in model for the cable bus block.
 */
public class CableBusModel implements IUnbakedGeometry<CableBusModel> {
    public static final ResourceLocation TRANSLUCENT_FACADE_MODEL = AppEng.makeId("part/translucent_facade");

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        var partModels = this.loadPartModels(baker, spriteGetter, modelState);

        var cableBuilder = new CableBuilder(spriteGetter);

        var translucentFacadeModel = baker.bake(TRANSLUCENT_FACADE_MODEL, modelState, spriteGetter);

        var facadeBuilder = new FacadeBuilder(baker, translucentFacadeModel);

        // This should normally not be used, but we *have* to provide a particle texture
        // or otherwise damage models will
        // crash
        var particleTexture = cableBuilder.getCoreTexture(CableCoreType.GLASS, AEColor.TRANSPARENT);

        return new CableBusBakedModel(cableBuilder, facadeBuilder, partModels, particleTexture);
    }

    private Map<ResourceLocation, BakedModel> loadPartModels(ModelBaker baker,
            Function<Material, TextureAtlasSprite> spriteGetterIn, ModelState transformIn) {
        ImmutableMap.Builder<ResourceLocation, BakedModel> result = ImmutableMap.builder();

        for (ResourceLocation location : PartModelsInternal.getModels()) {
            BakedModel bakedModel = baker.bake(location, transformIn, spriteGetterIn);
            if (bakedModel == null) {
                AELog.warn("Failed to bake part model {}", location);
            } else {
                result.put(location, bakedModel);
            }
        }

        return result.build();
    }
}
