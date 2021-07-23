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

import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderState.LineState;
import net.minecraft.client.renderer.RenderState.TransparencyState;

/**
 * This is based on the area render of https://github.com/TeamPneumatic/pnc-repressurized/
 */
public class OverlayRenderType extends RenderType {

    public OverlayRenderType(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn,
            boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn,
                clearTaskIn);
    }

    public static RenderType getBlockHilightFace() {
        return create("block_hilight",
                DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 65536,
                RenderType.State.builder()
                        .setTransparencyState(TransparencyState.CRUMBLING_TRANSPARENCY)
                        .setTextureState(NO_TEXTURE)
                        .setLightmapState(NO_LIGHTMAP)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .createCompositeState(false));
    }

    private static final LineState LINE_3 = new LineState(OptionalDouble.of(3.0));

    public static RenderType getBlockHilightLine() {
        return create("block_hilight_line",
                DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 65536,
                RenderType.State.builder().setLineState(LINE_3)
                        .setTransparencyState(TransparencyState.GLINT_TRANSPARENCY)
                        .setTextureState(NO_TEXTURE)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .setCullState(NO_CULL)
                        .setLightmapState(NO_LIGHTMAP)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .createCompositeState(false));
    }

    public static void finishBuffer(IRenderTypeBuffer buffer, RenderType type) {
        if (buffer instanceof IRenderTypeBuffer.Impl) {
            RenderSystem.disableDepthTest();
            ((IRenderTypeBuffer.Impl) buffer).endBatch(type);
        }
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
