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
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.render.model.IBakedModel;
import net.minecraft.client.render.model.IModelTransform;
import net.minecraft.client.render.model.IUnbakedModel;
import net.minecraft.client.render.model.ItemOverrideList;
import net.minecraft.client.render.model.Material;
import net.minecraft.client.render.model.ModelBakery;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import appeng.core.AppEng;

public class SpatialPylonModel implements IModelGeometry<SpatialPylonModel> {

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
            ItemOverrideList overrides, Identifier modelLocation) {
        Map<SpatialPylonTextureType, TextureAtlasSprite> textures = new EnumMap<>(SpatialPylonTextureType.class);

        for (SpatialPylonTextureType type : SpatialPylonTextureType.values()) {
            textures.put(type, spriteGetter.apply(getTexturePath(type)));
        }

        return new SpatialPylonBakedModel(textures);
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner,
                                            Function<Identifier, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Arrays.stream(SpatialPylonTextureType.values()).map(SpatialPylonModel::getTexturePath)
                .collect(Collectors.toList());
    }

    private static Material getTexturePath(SpatialPylonTextureType type) {
        return new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
                new Identifier(AppEng.MOD_ID, "block/spatial_pylon/" + type.name().toLowerCase()));
    }

}
