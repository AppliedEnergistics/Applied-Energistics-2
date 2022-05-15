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

import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.client.render.effects.ParticleTypes;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.AppEngClient;

/**
 * Renders the item currently being crafted by the molecular assembler, as well as the light strip when it's powered.
 */
@Environment(EnvType.CLIENT)
public class MolecularAssemblerRenderer implements BlockEntityRenderer<MolecularAssemblerBlockEntity> {

    public static final ResourceLocation LIGHTS_MODEL = AppEng.makeId("block/molecular_assembler_lights");

    private final RandomSource particleRandom = RandomSource.create();

    public MolecularAssemblerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MolecularAssemblerBlockEntity molecularAssembler, float partialTicks, PoseStack ms,
            MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

        AssemblerAnimationStatus status = molecularAssembler.getAnimationStatus();
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

        if (molecularAssembler.isPowered()) {
            renderPowerLight(ms, bufferIn, combinedLightIn, combinedOverlayIn);
        }
    }

    private void renderPowerLight(PoseStack ms, MultiBufferSource bufferIn, int combinedLightIn,
            int combinedOverlayIn) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel lightsModel = minecraft.getModelManager().bakedRegistry.get(LIGHTS_MODEL);
        if (lightsModel == null) {
            AELog.warn("Lights model missing");
            return;
        }

        // tripwire layer has the shader-property we're looking for:
        // alpha testing
        // translucency
        VertexConsumer buffer = bufferIn.getBuffer(RenderType.tripwire());

        // certainly doesn't use alpha testing, making it look like it will not work.
        minecraft.getBlockRenderer().getModelRenderer().renderModel(ms.last(), buffer, null,
                lightsModel, 1, 1, 1, combinedLightIn, combinedOverlayIn);
    }

    private void renderStatus(MolecularAssemblerBlockEntity molecularAssembler, PoseStack ms,
            MultiBufferSource bufferIn, int combinedLightIn, AssemblerAnimationStatus status) {
        double centerX = molecularAssembler.getBlockPos().getX() + 0.5f;
        double centerY = molecularAssembler.getBlockPos().getY() + 0.5f;
        double centerZ = molecularAssembler.getBlockPos().getZ() + 0.5f;

        // Spawn crafting FX that fly towards the block's center
        Minecraft minecraft = Minecraft.getInstance();
        if (status.getTicksUntilParticles() <= 0) {
            status.setTicksUntilParticles(4);

            if (AppEngClient.instance().shouldAddParticles(particleRandom)) {
                for (int x = 0; x < (int) Math.ceil(status.getSpeed() / 5.0); x++) {
                    minecraft.particleEngine.createParticle(ParticleTypes.CRAFTING, centerX, centerY, centerZ, 0, 0, 0);
                }
            }
        }

        ItemStack is = status.getIs();

        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5); // Translate to center of block

        if (!(is.getItem() instanceof BlockItem)) {
            ms.translate(0, -0.3f, 0);
        } else {
            ms.translate(0, -0.2f, 0);
        }

        itemRenderer.renderStatic(is, TransformType.GROUND, combinedLightIn,
                OverlayTexture.NO_OVERLAY, ms, bufferIn, 0);
        ms.popPose();
    }

}
