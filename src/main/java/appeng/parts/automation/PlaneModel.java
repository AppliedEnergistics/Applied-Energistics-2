/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.parts.automation;


import com.google.common.collect.Lists;
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
 * Built-in model for annihilation planes that supports connected textures.
 */
public class PlaneModel implements IModel {

    private final ResourceLocation frontTexture;
    private final ResourceLocation sidesTexture;
    private final ResourceLocation backTexture;
    private final PlaneConnections connections;

    public PlaneModel(ResourceLocation frontTexture, ResourceLocation sidesTexture, ResourceLocation backTexture, PlaneConnections connections) {
        this.frontTexture = frontTexture;
        this.sidesTexture = sidesTexture;
        this.backTexture = backTexture;
        this.connections = connections;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return Lists.newArrayList(this.frontTexture, this.sidesTexture, this.backTexture);
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        TextureAtlasSprite frontSprite = bakedTextureGetter.apply(this.frontTexture);
        TextureAtlasSprite sidesSprite = bakedTextureGetter.apply(this.sidesTexture);
        TextureAtlasSprite backSprite = bakedTextureGetter.apply(this.backTexture);

        return new PlaneBakedModel(format, frontSprite, sidesSprite, backSprite, this.connections);
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }

}
