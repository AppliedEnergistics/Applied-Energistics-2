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

import java.util.function.Function;
import java.util.stream.Stream;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;

import appeng.client.render.BasicUnbakedModel;

/**
 * Built-in model for annihilation planes that supports connected textures.
 */
public class PlaneModel implements BasicUnbakedModel<PlaneModel> {

    private final RenderMaterial frontTexture;
    private final RenderMaterial sidesTexture;
    private final RenderMaterial backTexture;

    public PlaneModel(ResourceLocation frontTexture, ResourceLocation sidesTexture, ResourceLocation backTexture) {
        this.frontTexture = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, frontTexture);
        this.sidesTexture = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, sidesTexture);
        this.backTexture = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, backTexture);
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
            Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
            ItemOverrideList overrides, ResourceLocation modelLocation) {
        TextureAtlasSprite frontSprite = spriteGetter.apply(this.frontTexture);
        TextureAtlasSprite sidesSprite = spriteGetter.apply(this.sidesTexture);
        TextureAtlasSprite backSprite = spriteGetter.apply(this.backTexture);

        return new PlaneBakedModel(frontSprite, sidesSprite, backSprite);
    }

    @Override
    public Stream<RenderMaterial> getAdditionalTextures() {
        return Stream.of(frontTexture, sidesTexture, backTexture);
    }

}
