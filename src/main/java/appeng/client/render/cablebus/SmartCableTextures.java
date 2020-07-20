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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

import appeng.core.AppEng;

/**
 * Manages the channel textures for smart cables.
 */
@Environment(EnvType.CLIENT)
public class SmartCableTextures {

    public static final SpriteIdentifier[] SMART_CHANNELS_TEXTURES = Arrays
            .stream(new Identifier[] { new Identifier(AppEng.MOD_ID, "part/cable/smart/channels_00"), //
                    new Identifier(AppEng.MOD_ID, "part/cable/smart/channels_01"), //
                    new Identifier(AppEng.MOD_ID, "part/cable/smart/channels_02"), //
                    new Identifier(AppEng.MOD_ID, "part/cable/smart/channels_03"), //
                    new Identifier(AppEng.MOD_ID, "part/cable/smart/channels_04"), //
                    new Identifier(AppEng.MOD_ID, "part/cable/smart/channels_10"), //
                    new Identifier(AppEng.MOD_ID, "part/cable/smart/channels_11"), //
                    new Identifier(AppEng.MOD_ID, "part/cable/smart/channels_12"), //
                    new Identifier(AppEng.MOD_ID, "part/cable/smart/channels_13"), //
                    new Identifier(AppEng.MOD_ID, "part/cable/smart/channels_14")//
            }).map(e -> new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, e)).toArray(SpriteIdentifier[]::new);

    // Textures used to display channels on smart cables. There's two sets of 5
    // textures each, and
    // one of each set are composed together to get even/odd colored channels
    private final Sprite[] textures;

    public SmartCableTextures(Function<SpriteIdentifier, Sprite> bakedTextureGetter) {
        this.textures = Arrays.stream(SMART_CHANNELS_TEXTURES)//
                .map(bakedTextureGetter)//
                .toArray(Sprite[]::new);
    }

    /**
     * The odd variant is used for displaying channels 1-4 as in use.
     */
    public Sprite getOddTextureForChannels(int channels) {
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
    public Sprite getEvenTextureForChannels(int channels) {
        if (channels < 5) {
            return this.textures[5];
        } else if (channels <= 8) {
            return this.textures[1 + channels];
        } else {
            return this.textures[9];
        }
    }
}
