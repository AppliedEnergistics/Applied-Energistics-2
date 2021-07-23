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

import java.util.Arrays;
import java.util.function.Function;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import appeng.core.AppEng;

/**
 * Manages the channel textures for smart cables.
 */
public class SmartCableTextures {

    public static final Material[] SMART_CHANNELS_TEXTURES = Arrays
            .stream(new ResourceLocation[] { new ResourceLocation(AppEng.MOD_ID, "part/cable/smart/channels_00"), //
                    new ResourceLocation(AppEng.MOD_ID, "part/cable/smart/channels_01"), //
                    new ResourceLocation(AppEng.MOD_ID, "part/cable/smart/channels_02"), //
                    new ResourceLocation(AppEng.MOD_ID, "part/cable/smart/channels_03"), //
                    new ResourceLocation(AppEng.MOD_ID, "part/cable/smart/channels_04"), //
                    new ResourceLocation(AppEng.MOD_ID, "part/cable/smart/channels_10"), //
                    new ResourceLocation(AppEng.MOD_ID, "part/cable/smart/channels_11"), //
                    new ResourceLocation(AppEng.MOD_ID, "part/cable/smart/channels_12"), //
                    new ResourceLocation(AppEng.MOD_ID, "part/cable/smart/channels_13"), //
                    new ResourceLocation(AppEng.MOD_ID, "part/cable/smart/channels_14")//
            }).map(e -> new Material(TextureAtlas.LOCATION_BLOCKS, e)).toArray(Material[]::new);

    // Textures used to display channels on smart cables. There's two sets of 5
    // textures each, and
    // one of each set are composed together to get even/odd colored channels
    private final TextureAtlasSprite[] textures;

    public SmartCableTextures(Function<Material, TextureAtlasSprite> bakedTextureGetter) {
        this.textures = Arrays.stream(SMART_CHANNELS_TEXTURES)//
                .map(bakedTextureGetter)//
                .toArray(TextureAtlasSprite[]::new);
    }

    /**
     * The odd variant is used for displaying channels 1-4 as in use.
     */
    public TextureAtlasSprite getOddTextureForChannels(int channels) {
        if (channels < 0) {
            return this.textures[0];
        } else if (channels <= 4) {
            return this.textures[channels];
        } else {
            return this.textures[4];
        }
    }

    /**
     * The odd variant is used for displaying channels 5-8 as in use.
     */
    public TextureAtlasSprite getEvenTextureForChannels(int channels) {
        if (channels < 5) {
            return this.textures[5];
        } else if (channels <= 8) {
            return this.textures[1 + channels];
        } else {
            return this.textures[9];
        }
    }
}
