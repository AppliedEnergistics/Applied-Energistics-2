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

import appeng.block.networking.CableCoreType;
import com.google.common.collect.ImmutableMap;
import com.mojang.math.Transformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.SimpleModelState;

import appeng.api.parts.PartModelsInternal;
import appeng.api.util.AEColor;
import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

/**
 * The built-in model for the cable bus block.
 */
public class CableBusModel implements BasicUnbakedModel {
    private static final Logger LOG = LoggerFactory.getLogger(CableBusModel.class);

    public static final ResourceLocation TRANSLUCENT_FACADE_MODEL = AppEng.makeId("part/translucent_facade");

    @Override
    public Collection<ResourceLocation> getDependencies() {
        PartModelsInternal.freeze();
        var models = new ArrayList<>(PartModelsInternal.getModels());
        models.add(TRANSLUCENT_FACADE_MODEL);
        return models;
    }

    @Override
    public BakedModel bake(TextureSlots textures, ModelBaker baker, ModelState modelState, boolean useAmbientOcclusion,
            boolean usesBlockLight, ItemTransforms itemTransforms, ContextMap additionalProperties) {
        var spriteGetter = baker.sprites();
        Map<ResourceLocation, BakedModel> partModels = this.loadPartModels(baker);

        CableBuilder cableBuilder = new CableBuilder(spriteGetter);

        BakedModel translucentFacadeModel = baker.bake(TRANSLUCENT_FACADE_MODEL,
                new SimpleModelState(Transformation.identity()));

        FacadeBuilder facadeBuilder = new FacadeBuilder(baker, translucentFacadeModel);

        // This should normally not be used, but we *have* to provide a particle texture
        // or otherwise damage models will
        // crash
        TextureAtlasSprite particleTexture = cableBuilder.getCoreTexture(CableCoreType.GLASS, AEColor.TRANSPARENT);

        return new CableBusBakedModel(cableBuilder, facadeBuilder, partModels, particleTexture);
    }

    private Map<ResourceLocation, BakedModel> loadPartModels(ModelBaker baker) {
        ImmutableMap.Builder<ResourceLocation, BakedModel> result = ImmutableMap.builder();

        var state = new SimpleModelState(Transformation.identity());
        for (var location : PartModelsInternal.getModels()) {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                var slots = UnbakedModel.getTopTextureSlots(baker.getModel(location), location::toString);
                if (slots.getMaterial("particle") == null) {
                    LOG.error("Part model {} is missing a 'particle' texture", location);
                }
            }

            var bakedModel = baker.bake(location, state);
            result.put(location, bakedModel);
        }

        return result.build();
    }
}
