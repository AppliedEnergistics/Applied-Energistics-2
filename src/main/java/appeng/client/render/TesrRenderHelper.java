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
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.IWideReadableNumberConverter;
import appeng.util.ReadableNumberConverter;

/**
 * Helper methods for rendering TESRs.
 */
public class TesrRenderHelper {

    private static final IWideReadableNumberConverter NUMBER_CONVERTER = ReadableNumberConverter.INSTANCE;

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

    // TODO, A different approach will have to be used for this from TESRs, -covers,
    // i have ideas.

    /**
     * Render an item in 2D.
     */
    public static void renderItem2d(PoseStack matrixStack, MultiBufferSource buffers, ItemStack itemStack,
            float scale, int combinedLightIn, int combinedOverlayIn) {
        if (!itemStack.isEmpty()) {
            matrixStack.pushPose();
            // Push it out of the block face a bit to avoid z-fighting
            matrixStack.translate(0, 0, 0.01f);
            // The Z-scaling by 0.0002 causes the model to be visually "flattened"
            // This cannot replace a proper projection, but it's cheap and gives the desired
            // effect at least from head-on
            matrixStack.scale(scale, scale, 0.0002f);

            Minecraft.getInstance().getItemRenderer().renderStatic(itemStack, TransformType.GUI,
                    combinedLightIn, OverlayTexture.NO_OVERLAY, matrixStack, buffers, 0);

            matrixStack.popPose();

        }
    }

    /**
     * Render an item in 2D and the given text below it.
     *
     * @param matrixStack
     * @param buffers
     * @param spacing           Specifies how far apart the item and the item stack amount are rendered.
     * @param combinedLightIn
     * @param combinedOverlayIn
     */
    public static void renderItem2dWithAmount(PoseStack matrixStack, MultiBufferSource buffers,
            IAEItemStack itemStack, float itemScale, float spacing, int combinedLightIn, int combinedOverlayIn) {
        final ItemStack renderStack = itemStack.asItemStackRepresentation();

        TesrRenderHelper.renderItem2d(matrixStack, buffers, renderStack, itemScale, combinedLightIn, combinedOverlayIn);

        final long stackSize = itemStack.getStackSize();
        final String renderedStackSize = NUMBER_CONVERTER.toWideReadableForm(stackSize);

        // Render the item count
        final Font fr = Minecraft.getInstance().font;
        final int width = fr.width(renderedStackSize);
        matrixStack.pushPose();
        matrixStack.translate(0.0f, spacing, 0.02f);
        matrixStack.scale(1.0f / 62.0f, -1.0f / 62.0f, 1.0f / 62.0f);
        matrixStack.scale(0.5f, 0.5f, 0);
        matrixStack.translate(-0.5f * width, 0.0f, 0.5f);
        fr.drawInBatch(renderedStackSize, 0, 0, -1, false, matrixStack.last().pose(), buffers, false, 0,
                15728880);
        matrixStack.popPose();

    }
}
