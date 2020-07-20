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

package appeng.client.render.model;

import appeng.client.render.BasicUnbakedModel;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Model class for the connected texture glass model.
 */
public class GlassModel implements BasicUnbakedModel {

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        return new GlassBakedModel(textureGetter);
    }

    @Override
    public Stream<SpriteIdentifier> getAdditionalTextures() {
        return ImmutableSet
                .<SpriteIdentifier>builder().add(GlassBakedModel.TEXTURE_A, GlassBakedModel.TEXTURE_B,
                        GlassBakedModel.TEXTURE_C, GlassBakedModel.TEXTURE_D)
                .add(GlassBakedModel.TEXTURES_FRAME).build().stream();
    }

}
