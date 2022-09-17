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


import appeng.client.render.cablebus.CubeBuilder;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Built-in model for annihilation planes that supports connected textures.
 */
public class PlaneBakedModel implements IBakedModel {

    private final TextureAtlasSprite frontTexture;

    private final List<BakedQuad> quads;

    PlaneBakedModel(VertexFormat format, TextureAtlasSprite frontTexture, TextureAtlasSprite sidesTexture, TextureAtlasSprite backTexture, PlaneConnections connections) {
        this.frontTexture = frontTexture;

        List<BakedQuad> quads = new ArrayList<>(4 * 6);

        CubeBuilder builder = new CubeBuilder(format, quads);

        builder.setTextures(sidesTexture, sidesTexture, frontTexture, backTexture, sidesTexture, sidesTexture);

        // Keep the orientation of the X axis in mind here. When looking at a quad facing north from the front,
        // The X-axis points left
        int minX = connections.isRight() ? 0 : 1;
        int maxX = connections.isLeft() ? 16 : 15;
        int minY = connections.isDown() ? 0 : 1;
        int maxY = connections.isUp() ? 16 : 15;

        builder.addCube(minX, minY, 0, maxX, maxY, 1);

        this.quads = ImmutableList.copyOf(quads);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side == null) {
            return this.quads;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.frontTexture;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
