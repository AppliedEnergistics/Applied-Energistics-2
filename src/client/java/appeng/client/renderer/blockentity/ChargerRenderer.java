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

package appeng.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.misc.ChargerBlockEntity;

/**
 * Renders the item being charged.
 */
public final class ChargerRenderer implements BlockEntityRenderer<ChargerBlockEntity, ChargerRenderState> {
    private final ItemModelResolver itemModelResolver;

    public ChargerRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public ChargerRenderState createRenderState() {
        return new ChargerRenderState();
    }

    @Override
    public void extractRenderState(ChargerBlockEntity be, ChargerRenderState state, float partialTicks, Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPos, crumblingOverlay);
        state.blockOrientation = BlockOrientation.get(be);

        var time = System.currentTimeMillis() / 1000.0;
        var yOffset = (float) Math.sin(time) * 0.02f;
        state.transform = new Transformation(new Vector3f(0.5f, 0.35f + yOffset, 0.5f), null, null, null);

        // TODO 1.21.9: Charger should implement ItemOwner
        state.item.clear();
        var item = be.getInternalInventory().getStackInSlot(0);
        if (!item.isEmpty()) {
            this.itemModelResolver.updateForTopItem(
                    state.item,
                    item,
                    ItemDisplayContext.GROUND,
                    be.getLevel(),
                    new ItemOwner() {
                        @Override
                        public Level level() {
                            return be.getLevel();
                        }

                        @Override
                        public Vec3 position() {
                            return be.getBlockPos().getCenter();
                        }

                        @Override
                        public float getVisualRotationYInDegrees() {
                            return 0;
                        }
                    },
                    // This is the random seed
                    (int) be.getBlockPos().asLong());
        }
    }

    @Override
    public void submit(ChargerRenderState state, PoseStack poseStack, SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {
        if (state.item.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(state.blockOrientation.getQuaternion());
        poseStack.translate(-0.5, -0.5, -0.5);

        poseStack.mulPose(state.transform.getMatrix());

        state.item.submit(poseStack, nodes, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }
}
