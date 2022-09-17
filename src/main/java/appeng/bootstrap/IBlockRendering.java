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


import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.BiFunction;


/**
 * Allows for client-side rendering to be customized in the context of block/item registration.
 */
public interface IBlockRendering {

    @SideOnly(Side.CLIENT)
    IBlockRendering modelCustomizer(BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> customizer);

    @SideOnly(Side.CLIENT)
    IBlockRendering blockColor(IBlockColor blockColor);

    @SideOnly(Side.CLIENT)
    IBlockRendering stateMapper(IStateMapper mapper);

    @SideOnly(Side.CLIENT)
    IBlockRendering tesr(TileEntitySpecialRenderer<?> tesr);

    /**
     * Registers a built-in model under the given resource path.
     */
    @SideOnly(Side.CLIENT)
    IBlockRendering builtInModel(String name, IModel model);

}
