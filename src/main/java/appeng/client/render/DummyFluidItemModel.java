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

package appeng.client.render;


import appeng.core.AppEng;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;


/**
 * The model class for facades. Since facades wrap existing models, they don't declare any dependencies here other
 * than the cable anchor.
 */
public class DummyFluidItemModel implements IModel {
    // We use this to get the default item transforms and make our lives easier
    private static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "item/dummy_fluid_item_base");

    private IModel baseModel = null;

    private IModel getBaseModel() {
        if (this.baseModel == null) {
            try {
                this.baseModel = ModelLoaderRegistry.getModel(MODEL_BASE);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return this.baseModel;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return Collections.emptyList();
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        IBakedModel bakedBaseModel = this.getBaseModel().bake(state, format, bakedTextureGetter);

        return new DummyFluidDispatcherBakedModel(bakedBaseModel, format, bakedTextureGetter);
    }

    @Override
    public IModelState getDefaultState() {
        return this.getBaseModel().getDefaultState();
    }
}
