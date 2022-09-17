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


import appeng.block.AEBaseTileBlock;
import appeng.bootstrap.components.BlockColorComponent;
import appeng.bootstrap.components.StateMapperComponent;
import appeng.bootstrap.components.TesrComponent;
import appeng.client.render.model.AutoRotatingModel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;


class BlockRendering implements IBlockRendering {

    @SideOnly(Side.CLIENT)
    private BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> modelCustomizer;

    @SideOnly(Side.CLIENT)
    private IBlockColor blockColor;

    @SideOnly(Side.CLIENT)
    private TileEntitySpecialRenderer<?> tesr;

    @SideOnly(Side.CLIENT)
    private IStateMapper stateMapper;

    @SideOnly(Side.CLIENT)
    private final Map<String, IModel> builtInModels = new HashMap<>();

    @Override
    @SideOnly(Side.CLIENT)
    public IBlockRendering modelCustomizer(BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> customizer) {
        this.modelCustomizer = customizer;
        return this;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IBlockRendering blockColor(IBlockColor blockColor) {
        this.blockColor = blockColor;
        return this;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IBlockRendering tesr(TileEntitySpecialRenderer<?> tesr) {
        this.tesr = tesr;
        return this;
    }

    @Override
    public IBlockRendering builtInModel(String name, IModel model) {
        this.builtInModels.put(name, model);
        return this;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IBlockRendering stateMapper(IStateMapper mapper) {
        this.stateMapper = mapper;
        return this;
    }

    void apply(FeatureFactory factory, Block block, Class<?> tileEntityClass) {
        if (this.tesr != null) {
            if (tileEntityClass == null) {
                throw new IllegalStateException("Tried to register a TESR for " + block + " even though no tile entity has been specified.");
            }
            factory.addBootstrapComponent(new TesrComponent(tileEntityClass, this.tesr));
        }

        if (this.modelCustomizer != null) {
            factory.addModelOverride(block.getRegistryName().getResourcePath(), this.modelCustomizer);
        } else if (block instanceof AEBaseTileBlock) {
            // This is a default rotating model if the base-block uses an AE tile entity which exposes UP/FRONT as
            // extended props
            factory.addModelOverride(block.getRegistryName().getResourcePath(), (l, m) -> new AutoRotatingModel(m));
        }

        // TODO : 1.12
        this.builtInModels.forEach(factory::addBuiltInModel);

        if (this.blockColor != null) {
            factory.addBootstrapComponent(new BlockColorComponent(block, this.blockColor));
        }

        if (this.stateMapper != null) {
            factory.addBootstrapComponent(new StateMapperComponent(block, this.stateMapper));
        }
    }
}
