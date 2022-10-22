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

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

/**
 * The built-in model for the cable bus block.
 */
@Environment(EnvType.CLIENT)
public class CableBusModel implements BasicUnbakedModel {

    public static final ResourceLocation TRANSLUCENT_FACADE_MODEL = AppEng.makeId("part/translucent_facade");

    @Override
    public Collection<ResourceLocation> getDependencies() {
        PartModelsInternal.freeze();
        var models = new ArrayList<>(PartModelsInternal.getModels());
        models.add(TRANSLUCENT_FACADE_MODEL);
        return models;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
    }

    @Nullable
    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelState, ResourceLocation resourceLocation) {
        Map<ResourceLocation, BakedModel> partModels = this.loadPartModels(baker, modelState);

        CableBuilder cableBuilder = new CableBuilder(spriteGetter);

        BakedModel translucentFacadeModel = baker.bake(TRANSLUCENT_FACADE_MODEL, modelState);

        FacadeBuilder facadeBuilder = new FacadeBuilder(baker, translucentFacadeModel);

        // This should normally not be used, but we *have* to provide a particle texture
        // or otherwise damage models will
        // crash
        TextureAtlasSprite particleTexture = cableBuilder.getCoreTexture(CableCoreType.GLASS, AEColor.TRANSPARENT);

        return new CableBusBakedModel(cableBuilder, facadeBuilder, partModels, particleTexture);
    }

    private Map<ResourceLocation, BakedModel> loadPartModels(ModelBaker baker, ModelState transformIn) {
        ImmutableMap.Builder<ResourceLocation, BakedModel> result = ImmutableMap.builder();

        for (ResourceLocation location : PartModelsInternal.getModels()) {
            BakedModel bakedModel = baker.bake(location, transformIn);
            if (bakedModel == null) {
                AELog.warn("Failed to bake part model {}", location);
            } else {
                result.put(location, bakedModel);
            }
        }

        return result.build();
    }
}
