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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

import appeng.api.util.AEColor;
import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

/**
 * Model wrapper for the memory card item model, which combines a base card layer with a "visual hash" of the part/tile.
 */
public class MemoryCardModel implements BasicUnbakedModel {

    public static final Map<AEColor, ResourceLocation> MODELS_BASE = new HashMap<>();
    private final AEColor memoryCardColor;

    static {
        for (AEColor color : AEColor.values()) {
            if (color != AEColor.TRANSPARENT) {
                MODELS_BASE.put(color,
                        new ResourceLocation(AppEng.MOD_ID, "item/memory_card_" + color.registryPrefix + "_base"));
            } else {
                MODELS_BASE.put(color, new ResourceLocation(AppEng.MOD_ID, "item/memory_card_base"));
            }
        }
    }

    private static final Material TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS,
            new ResourceLocation(AppEng.MOD_ID, "item/memory_card_hash"));

    public MemoryCardModel(AEColor memoryCardColor) {
        this.memoryCardColor = memoryCardColor;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.singleton(MODELS_BASE.get(memoryCardColor));
    }

    @Nullable
    @Override
    public BakedModel bake(ModelBaker loader, Function<Material, TextureAtlasSprite> textureGetter,
            ModelState rotationContainer, ResourceLocation modelId) {
        TextureAtlasSprite texture = textureGetter.apply(TEXTURE);

        BakedModel baseModel = loader.bake(MODELS_BASE.get(memoryCardColor), rotationContainer);

        return new MemoryCardBakedModel(baseModel, texture);
    }

}
