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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.core.AEConfig;
import appeng.core.particles.ParticleTypes;

/**
 * Renders the item currently being crafted by the molecular assembler, as well as the light strip when it's powered.
 */
public class MolecularAssemblerRenderer
        implements BlockEntityRenderer<MolecularAssemblerBlockEntity, MolecularAssemblerRenderState> {
    private final ItemModelResolver itemModelResolver;

    public MolecularAssemblerRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public MolecularAssemblerRenderState createRenderState() {
        return new MolecularAssemblerRenderState();
    }

    @Override
    public void extractRenderState(MolecularAssemblerBlockEntity be, MolecularAssemblerRenderState state,
            float partialTicks, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPos, crumblingOverlay);

        state.item.clear();

        var status = be.getAnimationStatus();
        if (status != null) {
            if (!Minecraft.getInstance().isPaused()) {
                if (status.isExpired()) {
                    be.setAnimationStatus(null);
                }

                status.setAccumulatedTicks(status.getAccumulatedTicks() + partialTicks);
                status.setTicksUntilParticles(status.getTicksUntilParticles() - partialTicks);
            }

            double centerX = be.getBlockPos().getX() + 0.5f;
            double centerY = be.getBlockPos().getY() + 0.5f;
            double centerZ = be.getBlockPos().getZ() + 0.5f;

            var is = status.getIs();

            // Spawn crafting FX that fly towards the block's center
            var level = be.getLevel();
            if (AEConfig.instance().isEnableEffects()) {
                if (status.getTicksUntilParticles() <= 0) {
                    status.setTicksUntilParticles(4);

                    for (int x = 0; x < (int) Math.ceil(status.getSpeed() / 5.0); x++) {
                        level.addParticle(new ItemParticleOption(ParticleTypes.CRAFTING, is), centerX, centerY, centerZ,
                                0,
                                0, 0);
                    }
                }
            }

            this.itemModelResolver.updateForTopItem(
                    state.item,
                    is,
                    ItemDisplayContext.FIXED,
                    be.getLevel(),
                    null,
                    // This is the random seed
                    (int) be.getBlockPos().asLong());
            state.blockItem = (is.getItem() instanceof BlockItem);
        }
    }

    @Override
    public void submit(MolecularAssemblerRenderState state, PoseStack poseStack, SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5); // Translate to center of block

        if (!state.blockItem) {
            poseStack.translate(0, -0.3f, 0);
        } else {
            poseStack.translate(0, -0.2f, 0);
        }

        state.item.submit(poseStack, nodes, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
