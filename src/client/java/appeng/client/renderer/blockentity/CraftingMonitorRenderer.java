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

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import appeng.api.stacks.AmountFormat;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.client.render.BlockEntityRenderHelper;

/**
 * Renders the item currently being crafted
 */
public class CraftingMonitorRenderer
        implements BlockEntityRenderer<CraftingMonitorBlockEntity, CraftingMonitorRenderState> {
    private final Font font;

    public CraftingMonitorRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
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
        state.textColor = be.getColor().contrastTextColor;
        state.textColor |= 0xFF000000; // ensure full visibility
        var jobProgress = be.getJobProgress();
        if (jobProgress != null) {
            int seed = (int) be.getBlockPos().asLong();
            state.what.extract(jobProgress.what(), be.getLevel(), seed);
            state.text = Component.literal(jobProgress.what().formatAmount(jobProgress.amount(), AmountFormat.SLOT))
                    .getVisualOrderText();
            state.textWidth = font.width(state.text);
        } else {
            state.what.clear();
            state.text = null;
            state.textWidth = 0;
        }
    }

    @Override
    public void submit(CraftingMonitorRenderState state, PoseStack poseStack, SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5); // Move to the center of the block
        BlockEntityRenderHelper.rotateToFace(poseStack, state.orientation);
        // Move to the "front" of the face.
        poseStack.translate(0, 0, 0.5);

        // On the texture, the actual "monitor" part of the block face is 6 out of 16 available pixels
        // the item has to fit in 4 of those, to leave space for the text. We move it up half a texel to create
        // space between it and the amount.
        float itemScale = 4 / 16f;

        BlockEntityRenderHelper.submitItem2dWithAmount(
                poseStack,
                state.what,
                state.text,
                state.textColor,
                state.textWidth,
                nodes, itemScale);

        poseStack.popPose();
    }
}
