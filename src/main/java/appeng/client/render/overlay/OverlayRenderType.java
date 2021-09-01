/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.client.render.overlay;

import java.util.OptionalDouble;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

/**
 * This is based on the area render of https://github.com/TeamPneumatic/pnc-repressurized/
 */
public class OverlayRenderType extends RenderType {

    private static RenderType BLOCK_HIGHLIGHT_FACE;
    private static RenderType BLOCK_HIGHLIGHT_LINE;

    public OverlayRenderType(String nameIn, VertexFormat formatIn, VertexFormat.Mode mode, int bufferSizeIn,
            boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, mode, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn,
                clearTaskIn);
    }

    public static RenderType getBlockHilightFace() {
        if (BLOCK_HIGHLIGHT_FACE == null) {
            BLOCK_HIGHLIGHT_FACE = create("block_hilight",
                    DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS, 65536, false, false,
                    CompositeState.builder()
                            .setTransparencyState(TransparencyStateShard.CRUMBLING_TRANSPARENCY)
                            .setTextureState(NO_TEXTURE)
                            .setLightmapState(NO_LIGHTMAP)
                            .setDepthTestState(NO_DEPTH_TEST)
                            .setWriteMaskState(COLOR_WRITE)
                            .setCullState(NO_CULL)
                            .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                            .createCompositeState(false));
        }
        return BLOCK_HIGHLIGHT_FACE;
    }

    private static final LineStateShard LINE_3 = new LineStateShard(OptionalDouble.of(3.0));

    public static RenderType getBlockHilightLine() {
        if (BLOCK_HIGHLIGHT_LINE == null) {
            BLOCK_HIGHLIGHT_LINE = create("block_hilight_line",
                    DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 65536, false, false,
                    CompositeState.builder().setLineState(LINE_3)
                            .setTransparencyState(TransparencyStateShard.GLINT_TRANSPARENCY)
                            .setTextureState(NO_TEXTURE)
                            .setDepthTestState(NO_DEPTH_TEST)
                            .setCullState(NO_CULL)
                            .setLightmapState(NO_LIGHTMAP)
                            .setWriteMaskState(COLOR_DEPTH_WRITE)
                            .setShaderState(RENDERTYPE_LINES_SHADER)
                            .createCompositeState(false));
        }
        return BLOCK_HIGHLIGHT_LINE;
    }

    public static int[] decomposeColor(int color) {
        int[] res = new int[4];
        res[0] = color >> 24 & 0xff;
        res[1] = color >> 16 & 0xff;
        res[2] = color >> 8 & 0xff;
        res[3] = color & 0xff;
        return res;
    }
}
