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

package appeng.client.render.spatial;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

public class SpatialPylonModel implements BasicUnbakedModel {

    @Override
    public BakedModel bake(ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform,
            ResourceLocation modelLocation) {
        Map<SpatialPylonTextureType, TextureAtlasSprite> textures = new EnumMap<>(SpatialPylonTextureType.class);

        for (SpatialPylonTextureType type : SpatialPylonTextureType.values()) {
            textures.put(type, spriteGetter.apply(getTexturePath(type)));
        }

        return new SpatialPylonBakedModel(textures);
    }

    @Override
    public Stream<Material> getAdditionalTextures() {
        return Arrays.stream(SpatialPylonTextureType.values()).map(SpatialPylonModel::getTexturePath);
    }

    private static Material getTexturePath(SpatialPylonTextureType type) {
        return new Material(TextureAtlas.LOCATION_BLOCKS,
                new ResourceLocation(AppEng.MOD_ID, "block/spatial_pylon/" + type.name().toLowerCase(Locale.ROOT)));
    }

}
