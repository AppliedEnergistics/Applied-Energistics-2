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


import appeng.api.util.AEColor;
import appeng.block.crafting.BlockCraftingMonitor;
import appeng.client.render.cablebus.CubeBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;


/**
 * The baked model for the crafting monitor. Please note that this model doesn't handle the item being displayed. That
 * is handled by a TESR.
 * Instead, this model adds 3 layered light textures using the [dark|medium|bright] color variants of the attached bus
 * color. The textures
 * are full-bright if the cube is powered.
 */
class MonitorBakedModel extends CraftingCubeBakedModel {

    private final TextureAtlasSprite chassisTexture;

    private final TextureAtlasSprite baseTexture;

    private final TextureAtlasSprite lightDarkTexture;

    private final TextureAtlasSprite lightMediumTexture;

    private final TextureAtlasSprite lightBrightTexture;

    MonitorBakedModel(VertexFormat format, TextureAtlasSprite ringCorner, TextureAtlasSprite ringHor, TextureAtlasSprite ringVer, TextureAtlasSprite chassisTexture, TextureAtlasSprite baseTexture, TextureAtlasSprite lightDarkTexture, TextureAtlasSprite lightMediumTexture, TextureAtlasSprite lightBrightTexture) {
        super(format, ringCorner, ringHor, ringVer);
        this.chassisTexture = chassisTexture;
        this.baseTexture = baseTexture;
        this.lightDarkTexture = lightDarkTexture;
        this.lightMediumTexture = lightMediumTexture;
        this.lightBrightTexture = lightBrightTexture;
    }

    @Override
    protected void addInnerCube(EnumFacing side, IBlockState state, CubeBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2) {
        EnumFacing forward = getForward(state);

        // For sides other than the front, use the chassis texture
        if (side != forward) {
            builder.setTexture(this.chassisTexture);
            builder.addCube(x1, y1, z1, x2, y2, z2);
            return;
        }

        builder.setTexture(this.baseTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);

        // Now add the three layered light textures
        AEColor color = getColor(state);
        boolean powered = state.getValue(BlockCraftingMonitor.POWERED);

        builder.setRenderFullBright(powered);

        builder.setColorRGB(color.whiteVariant);
        builder.setTexture(this.lightBrightTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);

        builder.setColorRGB(color.mediumVariant);
        builder.setTexture(this.lightMediumTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);

        builder.setColorRGB(color.blackVariant);
        builder.setTexture(this.lightDarkTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);

    }

    private static AEColor getColor(IBlockState state) {
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState extState = (IExtendedBlockState) state;
            AEColor color = extState.getValue(BlockCraftingMonitor.COLOR);
            if (color != null) {
                return color;
            }
        }

        return AEColor.TRANSPARENT;
    }

    private static EnumFacing getForward(IBlockState state) {
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState extState = (IExtendedBlockState) state;
            EnumFacing forward = extState.getValue(BlockCraftingMonitor.FORWARD);
            if (forward != null) {
                return forward;
            }
        }

        return EnumFacing.NORTH;
    }
}
