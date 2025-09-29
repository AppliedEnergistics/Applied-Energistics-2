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

package appeng.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;

import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.client.render.BlockEntityRenderHelper;

/**
 * Renders the item currently being crafted
 */
public class CraftingMonitorRenderer
        implements BlockEntityRenderer<CraftingMonitorBlockEntity, CraftingMonitorRenderState> {
    public CraftingMonitorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public CraftingMonitorRenderState createRenderState() {
        return new CraftingMonitorRenderState();
    }

    @Override
    public void extractRenderState(CraftingMonitorBlockEntity be, CraftingMonitorRenderState state, float partialTicks,
            Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPos, crumblingOverlay);

        state.orientation = be.getOrientation();
        state.jobProgress = be.getJobProgress();
        state.textColor = be.getColor().contrastTextColor;
    }

    @Override
    public void submit(CraftingMonitorRenderState state, PoseStack poseStack, SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {

        if (state.jobProgress != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5); // Move to the center of the block
            BlockEntityRenderHelper.rotateToFace(poseStack, state.orientation);
            poseStack.translate(0, 0.02, 0.5);

            // TODO 1.21.9
            BlockEntityRenderHelper.renderItem2dWithAmount(
                    poseStack,
                    null,
                    state.jobProgress.what(),
                    state.jobProgress.amount(), false,
                    0.3f,
                    -0.18f,
                    state.textColor,
                    null);

            poseStack.popPose();
        }
    }
}
