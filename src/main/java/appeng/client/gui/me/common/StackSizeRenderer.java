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

package appeng.client.gui.me.common;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;

import appeng.core.AEConfig;

/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class StackSizeRenderer {
    public static void renderSizeLabel(Font fontRenderer, float xPos, float yPos, String text) {
        renderSizeLabel(fontRenderer, xPos, yPos, text, AEConfig.instance().isUseLargeFonts());
    }

    public static void renderSizeLabel(Font fontRenderer, float xPos, float yPos, String text, boolean largeFonts) {
        final float scaleFactor = largeFonts ? 0.85f : 0.5f;

        Transformation tm = new Transformation(new Vector3f(0, 0, 300), // Taken from
                // ItemRenderer.renderItemOverlayIntoGUI
                null, new Vector3f(scaleFactor, scaleFactor, scaleFactor), null);
        renderSizeLabel(tm.getMatrix(), fontRenderer, xPos, yPos, text, largeFonts);
    }

    public static void renderSizeLabel(Matrix4f matrix, Font fontRenderer, float xPos, float yPos, String text,
            boolean largeFonts) {
        final float scaleFactor = largeFonts ? 0.85f : 0.5f;
        final float inverseScaleFactor = 1.0f / scaleFactor;
        final int offset = largeFonts ? 0 : -1;

        RenderSystem.disableBlend();
        final int X = (int) ((xPos + offset + 16.0f - fontRenderer.width(text) * scaleFactor)
                * inverseScaleFactor);
        final int Y = (int) ((yPos + offset + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
        BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        fontRenderer.drawInBatch(text, X, Y, 0xffffff, true, matrix, buffer, false, 0, 15728880);
        buffer.endBatch();
        RenderSystem.enableBlend();
    }

    public static void renderSizeLabel(PoseStack stack, Font fontRenderer, float xPos, float yPos, String text,
            boolean largeFonts) {
        final float scaleFactor = largeFonts ? 0.85f : 0.5f;

        stack.pushPose();
        stack.scale(scaleFactor, scaleFactor, scaleFactor);

        renderSizeLabel(stack.last().pose(), fontRenderer, xPos, yPos, text, largeFonts);

        stack.popPose();
    }

}
