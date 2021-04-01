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
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.ResourceLocation;
import appeng.block.AEBaseTileBlock;
import appeng.bootstrap.components.RenderTypeComponent;
import appeng.client.render.model.AutoRotatingBakedModel;

class BlockRendering implements IBlockRendering {

    private final ResourceLocation id;

    @Environment(EnvType.CLIENT)
    private BiFunction<ResourceLocation, IBakedModel, IBakedModel> modelCustomizer;

    @Environment(EnvType.CLIENT)
    private IBlockColor blockColor;

    @Environment(EnvType.CLIENT)
    private RenderType renderType;

    public BlockRendering(ResourceLocation id) {
        this.id = id;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public IBlockRendering modelCustomizer(BiFunction<ResourceLocation, IBakedModel, IBakedModel> customizer) {
        this.modelCustomizer = customizer;
        return this;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public IBlockRendering blockColor(IBlockColor blockColor) {
        this.blockColor = blockColor;
        return this;
    }

    @Override
    public IBlockRendering renderType(RenderType type) {
        this.renderType = type;
        return this;
    }

    void apply(FeatureFactory factory, Block block) {
        if (this.modelCustomizer != null) {
            factory.addModelOverride(id.getPath(), this.modelCustomizer);
        } else if (block instanceof AEBaseTileBlock) {
            // This is a default rotating model if the base-block uses an AE block entity
            // which exposes UP/FRONT as
            // extended props
            factory.addModelOverride(id.getPath(), (l, m) -> new AutoRotatingBakedModel(m));
        }

        if (this.blockColor != null) {
            ColorProviderRegistry.BLOCK.register(this.blockColor, block);
        }

        if (this.renderType != null) {
            factory.addBootstrapComponent(new RenderTypeComponent(block, this.renderType));
        }
    }
}
