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


import appeng.block.crafting.BlockCraftingUnit;
import appeng.client.render.cablebus.CubeBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;


/**
 * Crafting cube baked model that adds a full-bright light texture on top of a normal base texture onto the inner cube.
 * The light texture is only drawn fullbright if the multiblock is currently powered.
 */
class LightBakedModel extends CraftingCubeBakedModel {

    private final TextureAtlasSprite baseTexture;

    private final TextureAtlasSprite lightTexture;

    LightBakedModel(VertexFormat format, TextureAtlasSprite ringCorner, TextureAtlasSprite ringHor, TextureAtlasSprite ringVer, TextureAtlasSprite baseTexture, TextureAtlasSprite lightTexture) {
        super(format, ringCorner, ringHor, ringVer);
        this.baseTexture = baseTexture;
        this.lightTexture = lightTexture;
    }

    @Override
    protected void addInnerCube(EnumFacing facing, IBlockState state, CubeBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2) {
        builder.setTexture(this.baseTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);

        boolean powered = state.getValue(BlockCraftingUnit.POWERED);
        builder.setRenderFullBright(powered);
        builder.setTexture(this.lightTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);
    }
}
