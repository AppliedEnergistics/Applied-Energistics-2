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

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import appeng.api.util.AEColor;
import appeng.client.render.BasicUnbakedModel;
import appeng.core.AELog;
import appeng.core.features.registries.PartModels;

/**
 * The built-in model for the cable bus block.
 */
@Environment(EnvType.CLIENT)
public class CableBusModel implements BasicUnbakedModel {

    private final PartModels partModels;

    public CableBusModel(PartModels partModels) {
        this.partModels = partModels;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        partModels.setInitialized(true);
        return partModels.getModels();
    }

    @Override
    public Stream<RenderMaterial> getAdditionalTextures() {
        return CableBuilder.getTextures().stream();
    }

    @Nullable
    @Override
    public IBakedModel bakeModel(ModelBakery loader, Function<RenderMaterial, TextureAtlasSprite> textureGetter,
            IModelTransform rotationContainer, ResourceLocation modelId) {
        Map<ResourceLocation, IBakedModel> partModels = this.loadPartModels(loader, rotationContainer);

        CableBuilder cableBuilder = new CableBuilder(textureGetter);
        FacadeBuilder facadeBuilder = new FacadeBuilder(loader);

        // This should normally not be used, but we *have* to provide a particle texture
        // or otherwise damage models will
        // crash
        TextureAtlasSprite particleTexture = cableBuilder.getCoreTexture(CableCoreType.GLASS, AEColor.TRANSPARENT);

        return new CableBusBakedModel(cableBuilder, facadeBuilder, partModels, particleTexture);
    }

    private Map<ResourceLocation, IBakedModel> loadPartModels(ModelBakery loader, IModelTransform rotationContainer) {
        ImmutableMap.Builder<ResourceLocation, IBakedModel> result = ImmutableMap.builder();

        for (ResourceLocation location : this.partModels.getModels()) {
            IBakedModel bakedModel = loader.bake(location, rotationContainer);
            if (bakedModel == null) {
                AELog.warn("Failed to bake part model {}", location);
            } else {
                result.put(location, bakedModel);
            }
        }

        return result.build();
    }
}
