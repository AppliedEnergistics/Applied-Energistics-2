/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.client.AppEngClient;
import appeng.client.renderer.parts.PartRendererDispatcher;

/**
 * Renders dynamic aspects of parts attached to a cable bus.
 */
public class CableBusRenderer implements BlockEntityRenderer<CableBusBlockEntity, CableBusDynamicRenderState> {

    private final PartRendererDispatcher partRendererDispatcher;

    public CableBusRenderer(BlockEntityRendererProvider.Context context) {
        partRendererDispatcher = AppEngClient.instance().getPartRendererDispatcher();
    }

    @Override
    public CableBusDynamicRenderState createRenderState() {
        return new CableBusDynamicRenderState();
    }

    @Override
    public void extractRenderState(CableBusBlockEntity be, CableBusDynamicRenderState state, float partialTicks,
            Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPos, crumblingOverlay);
    }

    @Override
    public void submit(CableBusDynamicRenderState state, PoseStack poseStack, SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {
    }
// TODO 1.21.9
//    @Override
//    public void render(CableBusBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
//            int packedLight, int packedOverlay, Vec3 cameraPosition) {
//
//        var hasDynamicRenderers = be.getPartRendererCache(Boolean.class);
//
//        // Determine if there are any renderers for us
//        if (hasDynamicRenderers == null) {
//            hasDynamicRenderers = false;
//            for (var facing : IPart.ATTACHMENT_POINTS) {
//                var part = be.getPart(facing);
//                if (part != null) {
//                    var renderer = partRendererDispatcher.getRenderer(part);
//                    if (renderer != null) {
//                        hasDynamicRenderers = true;
//                        break;
//                    }
//                }
//            }
//            be.setPartRendererCache(hasDynamicRenderers);
//        }
//
//        if (hasDynamicRenderers) {
//            for (var facing : IPart.ATTACHMENT_POINTS) {
//                var part = be.getPart(facing);
//                if (part != null) {
//                    renderPart(part, partialTicks, poseStack, buffers, packedLight, packedOverlay, cameraPosition);
//                }
//            }
//        }
//
//    }
//
//    private <T extends IPart> void renderPart(T part,
//            float partialTicks,
//            PoseStack poseStack,
//            MultiBufferSource buffers,
//            int packedLight,
//            int packedOverlay,
//            Vec3 cameraPosition) {
//        var renderer = partRendererDispatcher.getRenderer(part);
//        if (renderer != null) {
//            renderer.renderDynamic(part, partialTicks, poseStack, buffers, packedLight, packedOverlay, cameraPosition);
//        }
//    }

    @Override
    public int getViewDistance() {
        return 900;
    }

}
