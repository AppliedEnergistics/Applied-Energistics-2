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

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.client.render.BlockEntityRenderHelper;

/**
 * Renders the item currently being crafted
 */
@Environment(EnvType.CLIENT)
public class CraftingMonitorRenderer implements BlockEntityRenderer<CraftingMonitorBlockEntity> {

    public CraftingMonitorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CraftingMonitorBlockEntity be, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffers, int combinedLight, int combinedOverlay) {

        var orientation = be.getOrientation();
        var jobProgress = be.getJobProgress();

        if (jobProgress != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5); // Move to the center of the block
            BlockEntityRenderHelper.rotateToFace(poseStack, orientation);
            poseStack.translate(0, 0.02, 0.5);

            BlockEntityRenderHelper.renderItem2dWithAmount(
                    poseStack,
                    buffers,
                    jobProgress.what(),
                    jobProgress.amount(),
                    0.3f,
                    -0.18f,
                    be.getColor().contrastTextColor);

            poseStack.popPose();
        }
    }
}
