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

import appeng.client.render.model.AutoRotatingBakedModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.Identifier;

import appeng.block.AEBaseTileBlock;
import appeng.bootstrap.components.BlockColorComponent;
import appeng.bootstrap.components.RenderTypeComponent;

class BlockRendering implements IBlockRendering {

    private final Identifier id;

    @Environment(EnvType.CLIENT)
    private BiFunction<Identifier, BakedModel, BakedModel> modelCustomizer;

    @Environment(EnvType.CLIENT)
    private BlockColorProvider blockColor;

    @Environment(EnvType.CLIENT)
    private RenderLayer renderType;

    public BlockRendering(Identifier id) {
        this.id = id;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public IBlockRendering modelCustomizer(BiFunction<Identifier, BakedModel, BakedModel> customizer) {
        this.modelCustomizer = customizer;
        return this;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public IBlockRendering blockColor(BlockColorProvider blockColor) {
        this.blockColor = blockColor;
        return this;
    }

    @Override
    public IBlockRendering renderType(RenderLayer type) {
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
