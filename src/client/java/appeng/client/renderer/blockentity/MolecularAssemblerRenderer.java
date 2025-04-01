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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import appeng.blockentity.crafting.MolecularAssemblerAnimationStatus;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.core.AppEng;
import appeng.core.AppEngClient;
import appeng.core.particles.ParticleTypes;

/**
 * Renders the item currently being crafted by the molecular assembler, as well as the light strip when it's powered.
 */
public class MolecularAssemblerRenderer implements BlockEntityRenderer<MolecularAssemblerBlockEntity> {

    public static final StandaloneModelKey<SimpleModelWrapper> LIGHTS_MODEL = new StandaloneModelKey<>(
            AppEng.makeId("block/molecular_assembler_lights"));

    private final RandomSource particleRandom = RandomSource.create();

    public MolecularAssemblerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MolecularAssemblerBlockEntity molecularAssembler, float partialTicks, PoseStack ms,
            MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, Vec3 cameraPosition) {

        MolecularAssemblerAnimationStatus status = molecularAssembler.getAnimationStatus();
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

    private void renderPowerLight(PoseStack ms, MultiBufferSource bufferIn, int packedLight,
            int packedOverlay) {
        Minecraft minecraft = Minecraft.getInstance();
        var lightsModel = minecraft.getModelManager().getStandaloneModel(LIGHTS_MODEL);
        // tripwire layer has the shader-property we're looking for:
        // alpha testing
        // translucency
        var pose = ms.last();
        var buffer = bufferIn.getBuffer(RenderType.tripwire());
        for (var quad : lightsModel.quads().getAll()) {
            buffer.putBulkData(pose, quad, 1f, 1f, 1f, 1f, packedLight, packedOverlay);
        }
    }

    private void renderStatus(MolecularAssemblerBlockEntity molecularAssembler, PoseStack ms,
            MultiBufferSource bufferIn, int combinedLightIn, MolecularAssemblerAnimationStatus status) {
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

        itemRenderer.renderStatic(is, ItemDisplayContext.GROUND, combinedLightIn,
                OverlayTexture.NO_OVERLAY, ms, bufferIn, molecularAssembler.getLevel(), 0);
        ms.popPose();
    }

}
