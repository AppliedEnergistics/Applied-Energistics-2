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

import appeng.api.parts.IPart;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.client.AppEngClient;
import appeng.client.api.renderer.parts.PartDynamicRenderState;
import appeng.client.api.renderer.parts.PartRenderer;
import appeng.client.renderer.parts.PartRendererDispatcher;

/**
 * Renders dynamic aspects of parts attached to a cable bus.
 */
public class CableBusRenderer implements BlockEntityRenderer<CableBusBlockEntity, CableBusDynamicRenderState> {
    private final PartRendererDispatcher renderers;

    public CableBusRenderer(BlockEntityRendererProvider.Context context) {
        this.renderers = AppEngClient.instance().getPartRendererDispatcher();
    }

    @Override
    public CableBusDynamicRenderState createRenderState() {
        return new CableBusDynamicRenderState();
    }

    @Override
    public void extractRenderState(CableBusBlockEntity be, CableBusDynamicRenderState state, float partialTicks,
            Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPos, crumblingOverlay);

        var hasDynamicRenderers = be.getPartRendererCache(Boolean.class);

        // Determine if there are any renderers for us
        if (hasDynamicRenderers == null) {
            hasDynamicRenderers = false;
            for (var facing : IPart.ATTACHMENT_POINTS) {
                var part = be.getPart(facing);
                if (part != null) {
                    var renderer = renderers.getRenderer(part.getClass());
                    if (renderer != null) {
                        hasDynamicRenderers = true;
                        break;
                    }
                }
            }
            be.setPartRendererCache(hasDynamicRenderers);
        }

        if (hasDynamicRenderers) {
            var attachmentPoints = IPart.ATTACHMENT_POINTS;
            state.sides = new PartDynamicRenderState[attachmentPoints.size()];

            for (int i = 0; i < attachmentPoints.size(); i++) {
                var facing = attachmentPoints.get(i);
                var part = be.getPart(facing);
                if (part != null) {
                    var sideState = extractPartState(state.sides[i], part, partialTicks);
                    if (sideState != null) {
                        sideState.lightCoords = state.lightCoords;
                    }
                    state.sides[i] = sideState;
                } else {
                    state.sides[i] = null;
                }
            }
        } else {
            state.sides = null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends IPart> PartDynamicRenderState extractPartState(
            @Nullable PartDynamicRenderState previousState,
            T part,
            float partialTicks) {
        var renderer = (PartRenderer<T, ?>) renderers.getRenderer(part.getClass());
        if (renderer != null) {
            return extractPartState(renderer, previousState, part, partialTicks);
        }
        return null;
    }

    private <T extends IPart, S extends PartDynamicRenderState> S extractPartState(
            PartRenderer<T, S> renderer,
            @Nullable PartDynamicRenderState previousState,
            T part,
            float partialTicks) {

        S state;
        Class<S> stateClass = renderer.stateClass();
        if (stateClass.isInstance(previousState)) {
            state = stateClass.cast(previousState);
        } else {
            state = renderer.createState();
        }
        state.renderer = renderer;

        renderer.extract(part, state, partialTicks);

        return state;
    }

    @Override
    public void submit(CableBusDynamicRenderState state, PoseStack poseStack, SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {

        if (state.sides == null) {
            return;
        }

        for (var sideState : state.sides) {
            if (sideState == null) {
                continue;
            }
            submitSide(sideState.renderer, sideState, poseStack, nodes, cameraRenderState);
        }
    }

    private <T extends IPart, S extends PartDynamicRenderState> void submitSide(PartRenderer<T, S> renderer,
            PartDynamicRenderState sideState,
            PoseStack poseStack,
            SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {
        var partSideState = renderer.stateClass().cast(sideState);
        renderer.submit(
                partSideState,
                poseStack,
                nodes,
                cameraRenderState);
    }

    @Override
    public int getViewDistance() {
        return 900;
    }

}
