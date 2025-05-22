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

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import appeng.client.render.BasicUnbakedModel;

/**
 * The built-in model for the connected texture crafting cube.
 */
public class CraftingCubeModel implements BasicUnbakedModel {
    private final AbstractCraftingUnitModelProvider<?> provider;

    public CraftingCubeModel(AbstractCraftingUnitModelProvider<?> provider) {
        this.provider = provider;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BakedModel bake(ModelBaker loader, Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelState, ResourceLocation modelId) {
        return this.provider.getBakedModel(spriteGetter);
    }
}
