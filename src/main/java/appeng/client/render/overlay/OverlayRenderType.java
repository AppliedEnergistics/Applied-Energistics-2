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

/**
 * This is based on the area render of https://github.com/TeamPneumatic/pnc-repressurized/
 */
public class OverlayRenderType extends RenderType {

    public OverlayRenderType(String p_i225992_1_, VertexFormat p_i225992_2_, int p_i225992_3_, int p_i225992_4_,
            boolean p_i225992_5_, boolean p_i225992_6_, Runnable p_i225992_7_, Runnable p_i225992_8_) {
        super(p_i225992_1_, p_i225992_2_, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, p_i225992_7_,
                p_i225992_8_);
    }

    public static RenderType getBlockHilightFace() {
        return makeType("block_hilight",
                DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
                RenderType.State.getBuilder()
                        .transparency(TransparencyState.CRUMBLING_TRANSPARENCY)
                        .texture(NO_TEXTURE)
                        .lightmap(LIGHTMAP_DISABLED)
                        .depthTest(DEPTH_ALWAYS)
                        .writeMask(COLOR_WRITE)
                        .cull(CULL_DISABLED)
                        .build(false));
    }

    private static final LineState LINE_3 = new LineState(OptionalDouble.of(3.0));

    public static RenderType getBlockHilightLine() {
        return makeType("block_hilight_line",
                DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
                RenderType.State.getBuilder().line(LINE_3)
                        .transparency(TransparencyState.GLINT_TRANSPARENCY)
                        .texture(NO_TEXTURE)
                        .depthTest(DEPTH_ALWAYS)
                        .cull(CULL_DISABLED)
                        .lightmap(LIGHTMAP_DISABLED)
                        .writeMask(COLOR_DEPTH_WRITE)
                        .build(false));
    }

    public static void finishBuffer(IRenderTypeBuffer buffer, RenderType type) {
        if (buffer instanceof IRenderTypeBuffer.Impl) {
            RenderSystem.disableDepthTest();
            ((IRenderTypeBuffer.Impl) buffer).finish(type);
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
