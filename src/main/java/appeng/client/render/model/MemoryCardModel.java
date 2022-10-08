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
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;

import appeng.api.util.AEColor;
import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

/**
 * Model wrapper for the memory card item model, which combines a base card layer with a "visual hash" of the
 * part/block.
 */
public class MemoryCardModel implements BasicUnbakedModel<MemoryCardModel> {

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
    public Collection<ResourceLocation> getModelDependencies() {
        return Collections.singleton(MODELS_BASE.get(memoryCardColor));
    }

    @Nullable
    @Override
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform,
            ItemOverrides overrides, ResourceLocation modelLocation) {
        TextureAtlasSprite texture = spriteGetter.apply(TEXTURE);

        BakedModel baseModel = bakery.bake(MODELS_BASE.get(memoryCardColor), modelTransform, spriteGetter);

        return new MemoryCardBakedModel(baseModel, texture);
    }

    @Override
    public Stream<Material> getAdditionalTextures() {
        return Stream.of(TEXTURE);
    }

}
