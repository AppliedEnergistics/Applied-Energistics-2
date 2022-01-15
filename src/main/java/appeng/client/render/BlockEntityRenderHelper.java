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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;

import appeng.api.client.AEStackRendering;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;

/**
 * Helper methods for rendering block entities.
 */
public class BlockEntityRenderHelper {

    /**
     * Rotate the current coordinate system so it is on the face of the given block side. This can be used to render on
     * the given face as if it was a 2D canvas.
     */
    public static void rotateToFace(PoseStack mStack, Direction face, byte spin) {
        switch (face) {
            case UP:
                mStack.mulPose(Vector3f.XP.rotationDegrees(270));
                mStack.mulPose(Vector3f.ZP.rotationDegrees(-spin * 90.0F));
                break;

            case DOWN:
                mStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
                mStack.mulPose(Vector3f.ZP.rotationDegrees(spin * -90.0F));
                break;

            case EAST:
                mStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
                break;

            case WEST:
                mStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
                break;

            case NORTH:
                mStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
                break;

            case SOUTH:
                break;

            default:
                break;
        }
    }

    /**
     * Render an item in 2D.
     */
    public static void renderItem2d(PoseStack poseStack,
            MultiBufferSource buffers,
            AEKey what,
            float scale,
            int combinedLightIn) {
        AEStackRendering.drawOnBlockFace(
                poseStack,
                buffers,
                what,
                scale,
                combinedLightIn);
    }

    /**
     * Render an item in 2D and the given text below it.
     *
     * @param spacing Specifies how far apart the item and the item stack amount are rendered.
     */
    public static void renderItem2dWithAmount(PoseStack poseStack,
            MultiBufferSource buffers,
            AEKey what,
            long amount,
            float itemScale,
            float spacing,
            int textColor) {
        renderItem2d(poseStack, buffers, what, itemScale, LightTexture.FULL_BRIGHT);

        var renderedStackSize = what.formatAmount(amount, AmountFormat.PREVIEW_REGULAR);

        // Render the item count
        var fr = Minecraft.getInstance().font;
        var width = fr.width(renderedStackSize);
        poseStack.pushPose();
        poseStack.translate(0.0f, spacing, 0.02f);
        poseStack.scale(1.0f / 62.0f, -1.0f / 62.0f, 1.0f / 62.0f);
        poseStack.scale(0.5f, 0.5f, 0);
        poseStack.translate(-0.5f * width, 0.0f, 0.5f);
        fr.drawInBatch(renderedStackSize, 0, 0, textColor, false, poseStack.last().pose(), buffers, false, 0,
                LightTexture.FULL_BRIGHT);
        poseStack.popPose();

    }
}
