/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.parts.automation;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;

import appeng.client.render.BasicUnbakedModel;

/**
 * Built-in model for annihilation planes that supports connected textures.
 */
public class PlaneModel implements BasicUnbakedModel {

    private final Material frontTexture;
    private final Material sidesTexture;
    private final Material backTexture;

    public PlaneModel(ResourceLocation frontTexture, ResourceLocation sidesTexture, ResourceLocation backTexture) {
        this.frontTexture = new Material(TextureAtlas.LOCATION_BLOCKS, frontTexture);
        this.sidesTexture = new Material(TextureAtlas.LOCATION_BLOCKS, sidesTexture);
        this.backTexture = new Material(TextureAtlas.LOCATION_BLOCKS, backTexture);
    }

    @Override
    public BakedModel bake(TextureSlots textures, ModelBaker baker, ModelState modelState, boolean useAmbientOcclusion,
            boolean usesBlockLight, ItemTransforms itemTransforms, ContextMap additionalProperties) {
        TextureAtlasSprite frontSprite = baker.sprites().get(this.frontTexture);
        TextureAtlasSprite sidesSprite = baker.sprites().get(this.sidesTexture);
        TextureAtlasSprite backSprite = baker.sprites().get(this.backTexture);

        return new PlaneBakedModel(frontSprite, sidesSprite, backSprite, itemTransforms);
    }

}
