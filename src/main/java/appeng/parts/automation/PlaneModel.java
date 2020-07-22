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

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

/**
 * Built-in model for annihilation planes that supports connected textures.
 */
public class PlaneModel implements IModelGeometry<PlaneModel> {

    private final RenderMaterial frontTexture;
    private final RenderMaterial sidesTexture;
    private final RenderMaterial backTexture;

    public PlaneModel(ResourceLocation frontTexture, ResourceLocation sidesTexture, ResourceLocation backTexture) {
        this.frontTexture = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, frontTexture);
        this.sidesTexture = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, sidesTexture);
        this.backTexture = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, backTexture);
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
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner,
            Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Arrays.asList(frontTexture, sidesTexture, backTexture);
    }

}
