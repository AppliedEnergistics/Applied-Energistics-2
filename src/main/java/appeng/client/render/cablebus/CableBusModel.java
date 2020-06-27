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
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.render.model.IBakedModel;
import net.minecraft.client.render.model.IModelTransform;
import net.minecraft.client.render.model.IUnbakedModel;
import net.minecraft.client.render.model.ItemOverrideList;
import net.minecraft.client.render.model.Material;
import net.minecraft.client.render.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import appeng.api.util.AEColor;
import appeng.core.AELog;
import appeng.core.features.registries.PartModels;

/**
 * The built-in model for the cable bus block.
 */
public class CableBusModel implements IModelGeometry<CableBusModel> {

    private final PartModels partModels;

    public CableBusModel(PartModels partModels) {
        this.partModels = partModels;
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
            ItemOverrideList overrides, Identifier modelLocation) {
        Map<Identifier, IBakedModel> partModels = this.loadPartModels(bakery, spriteGetter, modelTransform);

        CableBuilder cableBuilder = new CableBuilder(spriteGetter);
        FacadeBuilder facadeBuilder = new FacadeBuilder();

        // This should normally not be used, but we *have* to provide a particle texture
        // or otherwise damage models will
        // crash
        TextureAtlasSprite particleTexture = cableBuilder.getCoreTexture(CableCoreType.GLASS, AEColor.TRANSPARENT);

        return new CableBusBakedModel(cableBuilder, facadeBuilder, partModels, particleTexture);
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner,
                                            Function<Identifier, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Collections.unmodifiableList(CableBuilder.getTextures());
    }

    private Map<Identifier, IBakedModel> loadPartModels(ModelBakery bakery,
                                                        Function<Material, TextureAtlasSprite> spriteGetterIn, IModelTransform transformIn) {
        ImmutableMap.Builder<Identifier, IBakedModel> result = ImmutableMap.builder();

        for (Identifier location : this.partModels.getModels()) {
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
