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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import appeng.blockentity.crafting.MolecularAssemblerAnimationStatus;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.core.AEConfig;
import appeng.core.particles.ParticleTypes;

/**
 * Renders the item currently being crafted by the molecular assembler, as well as the light strip when it's powered.
 */
public class MolecularAssemblerRenderer implements BlockEntityRenderer<MolecularAssemblerBlockEntity> {
    public MolecularAssemblerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MolecularAssemblerBlockEntity molecularAssembler, float partialTicks, PoseStack ms,
            MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, Vec3 cameraPosition) {

        var status = molecularAssembler.getAnimationStatus();
        if (status != null) {
            if (!Minecraft.getInstance().isPaused()) {
                if (status.isExpired()) {
                    molecularAssembler.setAnimationStatus(null);
                }

                status.setAccumulatedTicks(status.getAccumulatedTicks() + partialTicks);
                status.setTicksUntilParticles(status.getTicksUntilParticles() - partialTicks);
            }

            renderStatus(molecularAssembler, ms, bufferIn, combinedLightIn, status);
        }
    }

    private void renderStatus(MolecularAssemblerBlockEntity be, PoseStack ms,
            MultiBufferSource bufferIn, int combinedLightIn, MolecularAssemblerAnimationStatus status) {
        double centerX = be.getBlockPos().getX() + 0.5f;
        double centerY = be.getBlockPos().getY() + 0.5f;
        double centerZ = be.getBlockPos().getZ() + 0.5f;

        ItemStack is = status.getIs();

        // Spawn crafting FX that fly towards the block's center
        var level = be.getLevel();
        var minecraft = Minecraft.getInstance();
        if (AEConfig.instance().isEnableEffects()) {
            if (status.getTicksUntilParticles() <= 0) {
                status.setTicksUntilParticles(4);

                for (int x = 0; x < (int) Math.ceil(status.getSpeed() / 5.0); x++) {
                    level.addParticle(new ItemParticleOption(ParticleTypes.CRAFTING, is), centerX, centerY, centerZ, 0,
                            0, 0);
                }
            }
        }

        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5); // Translate to center of block

        if (!(is.getItem() instanceof BlockItem)) {
            ms.translate(0, -0.3f, 0);
        } else {
            ms.translate(0, -0.2f, 0);
        }

        itemRenderer.renderStatic(is, ItemDisplayContext.GROUND, combinedLightIn,
                OverlayTexture.NO_OVERLAY, ms, bufferIn, be.getLevel(), 0);
        ms.popPose();
    }

}
