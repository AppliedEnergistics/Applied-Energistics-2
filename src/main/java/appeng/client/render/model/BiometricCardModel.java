/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

/**
 * Model wrapper for the biometric card item model, which combines a base card layer with a "visual hash" of the player
 * name
 */
public class BiometricCardModel implements BasicUnbakedModel {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "item/biometric_card_base");
    private static final RenderMaterial TEXTURE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "item/biometric_card_hash"));

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.singleton(MODEL_BASE);
    }

    @Override
    public Stream<RenderMaterial> getAdditionalTextures() {
        return Stream.of(TEXTURE);
    }

    @Nullable
    @Override
    public IBakedModel bakeModel(ModelBakery loader, Function<RenderMaterial, TextureAtlasSprite> textureGetter,
            IModelTransform rotationContainer, ResourceLocation modelId) {
        TextureAtlasSprite texture = textureGetter.apply(TEXTURE);

        IBakedModel baseModel = loader.bake(MODEL_BASE, rotationContainer);

        return new BiometricCardBakedModel(baseModel, texture);
    }

}
