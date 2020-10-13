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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;

import appeng.api.util.AEColor;
import appeng.client.render.BasicUnbakedModel;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.features.registries.PartModels;

/**
 * The built-in model for the cable bus block.
 */
public class CableBusModel implements BasicUnbakedModel<CableBusModel> {

    public static final ResourceLocation TRANSLUCENT_FACADE_MODEL = AppEng.makeId("part/translucent_facade");

    private final PartModels partModels;

    public CableBusModel(PartModels partModels) {
        this.partModels = partModels;
    }

    @Override
    public Collection<ResourceLocation> getModelDependencies() {
        partModels.setInitialized(true);
        List<ResourceLocation> models = new ArrayList<>(partModels.getModels());
        models.add(TRANSLUCENT_FACADE_MODEL);
        return models;
    }

    @Override
    public Stream<RenderMaterial> getAdditionalTextures() {
        return CableBuilder.getTextures().stream();
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
            Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
            ItemOverrideList overrides, ResourceLocation modelLocation) {
        Map<ResourceLocation, IBakedModel> partModels = this.loadPartModels(bakery, spriteGetter, modelTransform);

        CableBuilder cableBuilder = new CableBuilder(spriteGetter);

        IBakedModel translucentFacadeModel = bakery.getBakedModel(TRANSLUCENT_FACADE_MODEL, modelTransform,
                spriteGetter);

        FacadeBuilder facadeBuilder = new FacadeBuilder(translucentFacadeModel);

        // This should normally not be used, but we *have* to provide a particle texture
        // or otherwise damage models will
        // crash
        TextureAtlasSprite particleTexture = cableBuilder.getCoreTexture(CableCoreType.GLASS, AEColor.TRANSPARENT);

        return new CableBusBakedModel(cableBuilder, facadeBuilder, partModels, particleTexture);
    }

    private Map<ResourceLocation, IBakedModel> loadPartModels(ModelBakery bakery,
            Function<RenderMaterial, TextureAtlasSprite> spriteGetterIn, IModelTransform transformIn) {
        ImmutableMap.Builder<ResourceLocation, IBakedModel> result = ImmutableMap.builder();

        for (ResourceLocation location : this.partModels.getModels()) {
            IBakedModel bakedModel = bakery.getBakedModel(location, transformIn, spriteGetterIn);
            if (bakedModel == null) {
                AELog.warn("Failed to bake part model {}", location);
            } else {
                result.put(location, bakedModel);
            }
        }

        return result.build();
    }
}
