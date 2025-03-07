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

package appeng.client.render.tesr.spatial;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;

import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;
import net.minecraft.util.context.ContextMap;

public class SpatialPylonModel implements BasicUnbakedModel {

    @Override
    public BakedModel bake(TextureSlots textures, ModelBaker baker, ModelState modelState, boolean useAmbientOcclusion, boolean usesBlockLight, ItemTransforms itemTransforms, ContextMap additionalProperties) {
        Map<SpatialPylonTextureType, TextureAtlasSprite> pylonTextures = new EnumMap<>(SpatialPylonTextureType.class);

        for (SpatialPylonTextureType type : SpatialPylonTextureType.values()) {
            pylonTextures.put(type, baker.sprites().get(getTexturePath(type)));
        }

        return new SpatialPylonBakedModel(pylonTextures, itemTransforms);
    }

    private static Material getTexturePath(SpatialPylonTextureType type) {
        return new Material(TextureAtlas.LOCATION_BLOCKS,
                AppEng.makeId("block/spatial_pylon/" + type.name().toLowerCase(Locale.ROOT)));
    }

}
