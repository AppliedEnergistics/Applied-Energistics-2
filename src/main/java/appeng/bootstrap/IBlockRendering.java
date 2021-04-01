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

package appeng.bootstrap;

import java.util.function.BiFunction;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.ResourceLocation;

/**
 * Allows for client-side rendering to be customized in the context of block/item registration.
 */
public interface IBlockRendering {

    @Environment(EnvType.CLIENT)
    IBlockRendering modelCustomizer(BiFunction<ResourceLocation, IBakedModel, IBakedModel> customizer);

    @Environment(EnvType.CLIENT)
    IBlockRendering blockColor(IBlockColor blockColor);

    @Environment(EnvType.CLIENT)
    IBlockRendering renderType(RenderType type);

}
