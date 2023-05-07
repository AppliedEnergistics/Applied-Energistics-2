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

import org.joml.Quaternionf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import appeng.api.client.AEKeyRendering;
import appeng.api.orientation.BlockOrientation;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;

/**
 * Helper methods for rendering block entities.
 */
public final class BlockEntityRenderHelper {
    private BlockEntityRenderHelper() {
    }

    private static final Quaternionf ROTATE_TO_FRONT;

    static {
        ROTATE_TO_FRONT = new Quaternionf().rotationY(Mth.DEG_TO_RAD * 180);
    }

    /**
     * Rotate the current coordinate system, so it is on the face of the given block side. This can be used to render on
     * the given face as if it was a 2D canvas, where x+ is facing right and y+ is facing up.
     */
    public static void rotateToFace(PoseStack stack, BlockOrientation orientation) {
        stack.mulPose(orientation.getQuaternion());
        stack.mulPose(ROTATE_TO_FRONT);
    }

    /**
     * Render an item in 2D.
     */
    public static void renderItem2d(PoseStack poseStack,
            MultiBufferSource buffers,
            AEKey what,
            float scale,
            int combinedLightIn, Level level) {
        AEKeyRendering.drawOnBlockFace(
                poseStack,
                buffers,
                what,
                scale,
                combinedLightIn, level);
    }

    /**
     * Render an item in 2D and the given text below it.
     *
     * @param spacing Specifies how far apart the item and the item stack amount are rendered.
     * @param level
     */
    public static void renderItem2dWithAmount(PoseStack poseStack,
            MultiBufferSource buffers,
            AEKey what,
            long amount,
            float itemScale,
            float spacing,
            int textColor, Level level) {
        renderItem2d(poseStack, buffers, what, itemScale, LightTexture.FULL_BRIGHT, level);

        var renderedStackSize = what.formatAmount(amount, AmountFormat.SLOT);

        // Render the item count
        var fr = Minecraft.getInstance().font;
        var width = fr.width(renderedStackSize);
        poseStack.pushPose();
        poseStack.translate(0.0f, spacing, 0.02f);
        poseStack.scale(1.0f / 62.0f, -1.0f / 62.0f, 1.0f / 62.0f);
        poseStack.scale(0.5f, 0.5f, 0);
        poseStack.translate(-0.5f * width, 0.0f, 0.5f);
        fr.drawInBatch(renderedStackSize, 0, 0, textColor, false, poseStack.last().pose(), buffers,
                Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        poseStack.popPose();

    }
}
