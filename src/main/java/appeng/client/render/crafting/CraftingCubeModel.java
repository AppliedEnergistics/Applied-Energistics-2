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

import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

/**
 * The built-in model for the connected texture crafting cube.
 */
public class CraftingCubeModel implements BasicUnbakedModel {

    private final static SpriteIdentifier RING_CORNER = texture("ring_corner");
    private final static SpriteIdentifier RING_SIDE_HOR = texture("ring_side_hor");
    private final static SpriteIdentifier RING_SIDE_VER = texture("ring_side_ver");
    private final static SpriteIdentifier UNIT_BASE = texture("unit_base");
    private final static SpriteIdentifier LIGHT_BASE = texture("light_base");
    private final static SpriteIdentifier ACCELERATOR_LIGHT = texture("accelerator_light");
    private final static SpriteIdentifier STORAGE_1K_LIGHT = texture("1k_storage_light");
    private final static SpriteIdentifier STORAGE_4K_LIGHT = texture("4k_storage_light");
    private final static SpriteIdentifier STORAGE_16K_LIGHT = texture("16k_storage_light");
    private final static SpriteIdentifier STORAGE_64K_LIGHT = texture("64k_storage_light");
    private final static SpriteIdentifier MONITOR_BASE = texture("monitor_base");
    private final static SpriteIdentifier MONITOR_LIGHT_DARK = texture("monitor_light_dark");
    private final static SpriteIdentifier MONITOR_LIGHT_MEDIUM = texture("monitor_light_medium");
    private final static SpriteIdentifier MONITOR_LIGHT_BRIGHT = texture("monitor_light_bright");

    private final AbstractCraftingUnitBlock.CraftingUnitType type;

    public CraftingCubeModel(AbstractCraftingUnitBlock.CraftingUnitType type) {
        this.type = type;
    }

    @Override
    public Stream<SpriteIdentifier> getAdditionalTextures() {
        return Stream.of(RING_CORNER, RING_SIDE_HOR, RING_SIDE_VER, UNIT_BASE, LIGHT_BASE, ACCELERATOR_LIGHT,
                STORAGE_1K_LIGHT, STORAGE_4K_LIGHT, STORAGE_16K_LIGHT, STORAGE_64K_LIGHT, MONITOR_BASE,
                MONITOR_LIGHT_DARK, MONITOR_LIGHT_MEDIUM, MONITOR_LIGHT_BRIGHT);
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter,
            ModelBakeSettings rotationContainer, Identifier modelId) {
        // Retrieve our textures and pass them on to the baked model
        Sprite ringCorner = textureGetter.apply(RING_CORNER);
        Sprite ringSideHor = textureGetter.apply(RING_SIDE_HOR);
        Sprite ringSideVer = textureGetter.apply(RING_SIDE_VER);

        switch (this.type) {
            case UNIT:
                return new UnitBakedModel(ringCorner, ringSideHor, ringSideVer, textureGetter.apply(UNIT_BASE));
            case ACCELERATOR:
            case STORAGE_1K:
            case STORAGE_4K:
            case STORAGE_16K:
            case STORAGE_64K:
                return new LightBakedModel(ringCorner, ringSideHor, ringSideVer, textureGetter.apply(LIGHT_BASE),
                        getLightTexture(textureGetter, this.type));
            case MONITOR:
                return new MonitorBakedModel(ringCorner, ringSideHor, ringSideVer, textureGetter.apply(UNIT_BASE),
                        textureGetter.apply(MONITOR_BASE), textureGetter.apply(MONITOR_LIGHT_DARK),
                        textureGetter.apply(MONITOR_LIGHT_MEDIUM), textureGetter.apply(MONITOR_LIGHT_BRIGHT));
            default:
                throw new IllegalArgumentException("Unsupported crafting unit type: " + this.type);
        }
    }

    private static Sprite getLightTexture(Function<SpriteIdentifier, Sprite> textureGetter,
            AbstractCraftingUnitBlock.CraftingUnitType type) {
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

    private static SpriteIdentifier texture(String name) {
        return new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX,
                new Identifier(AppEng.MOD_ID, "block/crafting/" + name));
    }
}
