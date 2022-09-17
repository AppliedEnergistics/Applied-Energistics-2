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

package appeng.client.render.crafting;


import appeng.block.crafting.BlockCraftingUnit;
import appeng.core.AppEng;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;


/**
 * The built-in model for the connected texture crafting cube.
 */
class CraftingCubeModel implements IModel {

    private final static ResourceLocation RING_CORNER = texture("ring_corner");
    private final static ResourceLocation RING_SIDE_HOR = texture("ring_side_hor");
    private final static ResourceLocation RING_SIDE_VER = texture("ring_side_ver");
    private final static ResourceLocation UNIT_BASE = texture("unit_base");
    private final static ResourceLocation LIGHT_BASE = texture("light_base");
    private final static ResourceLocation ACCELERATOR_LIGHT = texture("accelerator_light");
    private final static ResourceLocation STORAGE_1K_LIGHT = texture("storage_1k_light");
    private final static ResourceLocation STORAGE_4K_LIGHT = texture("storage_4k_light");
    private final static ResourceLocation STORAGE_16K_LIGHT = texture("storage_16k_light");
    private final static ResourceLocation STORAGE_64K_LIGHT = texture("storage_64k_light");
    private final static ResourceLocation MONITOR_BASE = texture("monitor_base");
    private final static ResourceLocation MONITOR_LIGHT_DARK = texture("monitor_light_dark");
    private final static ResourceLocation MONITOR_LIGHT_MEDIUM = texture("monitor_light_medium");
    private final static ResourceLocation MONITOR_LIGHT_BRIGHT = texture("monitor_light_bright");

    private final BlockCraftingUnit.CraftingUnitType type;

    CraftingCubeModel(BlockCraftingUnit.CraftingUnitType type) {
        this.type = type;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return ImmutableList.of(RING_CORNER, RING_SIDE_HOR, RING_SIDE_VER, UNIT_BASE, LIGHT_BASE, ACCELERATOR_LIGHT, STORAGE_1K_LIGHT, STORAGE_4K_LIGHT,
                STORAGE_16K_LIGHT, STORAGE_64K_LIGHT, MONITOR_BASE, MONITOR_LIGHT_DARK, MONITOR_LIGHT_MEDIUM, MONITOR_LIGHT_BRIGHT);
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        // Retrieve our textures and pass them on to the baked model
        TextureAtlasSprite ringCorner = bakedTextureGetter.apply(RING_CORNER);
        TextureAtlasSprite ringSideHor = bakedTextureGetter.apply(RING_SIDE_HOR);
        TextureAtlasSprite ringSideVer = bakedTextureGetter.apply(RING_SIDE_VER);

        switch (this.type) {
            case UNIT:
                return new UnitBakedModel(format, ringCorner, ringSideHor, ringSideVer, bakedTextureGetter.apply(UNIT_BASE));
            case ACCELERATOR:
            case STORAGE_1K:
            case STORAGE_4K:
            case STORAGE_16K:
            case STORAGE_64K:
                return new LightBakedModel(format, ringCorner, ringSideHor, ringSideVer, bakedTextureGetter
                        .apply(LIGHT_BASE), getLightTexture(bakedTextureGetter, this.type));
            case MONITOR:
                return new MonitorBakedModel(format, ringCorner, ringSideHor, ringSideVer, bakedTextureGetter.apply(UNIT_BASE), bakedTextureGetter
                        .apply(MONITOR_BASE), bakedTextureGetter.apply(
                        MONITOR_LIGHT_DARK), bakedTextureGetter.apply(MONITOR_LIGHT_MEDIUM), bakedTextureGetter.apply(MONITOR_LIGHT_BRIGHT));
            default:
                throw new IllegalArgumentException("Unsupported crafting unit type: " + this.type);
        }
    }

    private static TextureAtlasSprite getLightTexture(Function<ResourceLocation, TextureAtlasSprite> textureGetter, BlockCraftingUnit.CraftingUnitType type) {
        switch (type) {
            case ACCELERATOR:
                return textureGetter.apply(ACCELERATOR_LIGHT);
            case STORAGE_1K:
                return textureGetter.apply(STORAGE_1K_LIGHT);
            case STORAGE_4K:
                return textureGetter.apply(STORAGE_4K_LIGHT);
            case STORAGE_16K:
                return textureGetter.apply(STORAGE_16K_LIGHT);
            case STORAGE_64K:
                return textureGetter.apply(STORAGE_64K_LIGHT);
            default:
                throw new IllegalArgumentException("Crafting unit type " + type + " does not use a light texture.");
        }
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(AppEng.MOD_ID, "blocks/crafting/" + name);
    }
}
