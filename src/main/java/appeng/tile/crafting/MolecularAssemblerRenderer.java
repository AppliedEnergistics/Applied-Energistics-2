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

package appeng.tile.crafting;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import appeng.client.render.effects.ParticleTypes;
import appeng.core.AppEng;

/**
 * Renders the item currently being crafted by the molecular assembler, as well
 * as the light strip when it's powered.
 */
@OnlyIn(Dist.CLIENT)
public class MolecularAssemblerRenderer extends TileEntityRenderer<MolecularAssemblerTileEntity> {

    public static final ResourceLocation LIGHTS_MODEL = AppEng.makeId("block/molecular_assembler_lights");

    private static final RenderType MC_161917_RENDERTYPE_FIX = createRenderType();

    private final Random particleRandom = new Random();

    public MolecularAssemblerRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(MolecularAssemblerTileEntity molecularAssembler, float partialTicks, MatrixStack ms,
            IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

        AssemblerAnimationStatus status = molecularAssembler.getAnimationStatus();
        if (status != null) {
            if (!Minecraft.getInstance().isGamePaused()) {
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

    private void renderPowerLight(MatrixStack ms, IRenderTypeBuffer bufferIn, int combinedLightIn,
            int combinedOverlayIn) {
        // Render the translucent light overlay here instead of in the block, because
        // thanks to the following MC
        // bug, our particles would otherwise not be visible (because the glass pane
        // would also render as translucent,
        // even the fully transparent part)
        // https://bugs.mojang.com/browse/MC-161917
        Minecraft minecraft = Minecraft.getInstance();
        IBakedModel lightsModel = minecraft.getModelManager().getModel(LIGHTS_MODEL);
        IVertexBuilder buffer = bufferIn.getBuffer(MC_161917_RENDERTYPE_FIX);

        minecraft.getBlockRendererDispatcher().getBlockModelRenderer().renderModel(ms.getLast(), buffer, null,
                lightsModel, 1, 1, 1, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
    }

    private void renderStatus(MolecularAssemblerTileEntity molecularAssembler, MatrixStack ms,
            IRenderTypeBuffer bufferIn, int combinedLightIn, AssemblerAnimationStatus status) {
        double centerX = molecularAssembler.getPos().getX() + 0.5f;
        double centerY = molecularAssembler.getPos().getY() + 0.5f;
        double centerZ = molecularAssembler.getPos().getZ() + 0.5f;

        // Spawn crafting FX that fly towards the block's center
        Minecraft minecraft = Minecraft.getInstance();
        if (status.getTicksUntilParticles() <= 0) {
            status.setTicksUntilParticles(4);

            if (AppEng.proxy.shouldAddParticles(particleRandom)) {
                for (int x = 0; x < (int) Math.ceil(status.getSpeed() / 5.0); x++) {
                    minecraft.particles.addParticle(ParticleTypes.CRAFTING, centerX, centerY, centerZ, 0, 0, 0);
                }
            }
        }

        ItemStack is = status.getIs();

        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        ms.push();
        ms.translate(0.5, 0.5, 0.5); // Translate to center of block

        if (!(is.getItem().getItem() instanceof BlockItem)) {
            ms.translate(0, -0.3f, 0);
        } else {
            ms.translate(0, -0.2f, 0);
        }

        itemRenderer.renderItem(is, ItemCameraTransforms.TransformType.GROUND, combinedLightIn,
                OverlayTexture.NO_OVERLAY, ms, bufferIn);
        ms.pop();
    }

    /**
     * See above for when this can be removed. It creates a RenderType that is
     * equivalent to {@link RenderType#getTranslucent()}, but enables alpha testing.
     * This prevents the fully transparents parts of the rendered block model from
     * occluding our particles.
     */
    private static RenderType createRenderType() {
        RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY = ObfuscationReflectionHelper
                .getPrivateValue(RenderState.class, null, "field_228515_g_");
        RenderState.TextureState mipmapBlockAtlasTexture = new RenderState.TextureState(
                AtlasTexture.LOCATION_BLOCKS_TEXTURE, false, true);
        RenderState.LightmapState disableLightmap = new RenderState.LightmapState(false);
        RenderType.State glState = RenderType.State.getBuilder().texture(mipmapBlockAtlasTexture)
                .transparency(TRANSLUCENT_TRANSPARENCY).alpha(new RenderState.AlphaState(0.05F))
                .lightmap(disableLightmap).build(true);

        return RenderType.makeType("ae2_translucent_alphatest", DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP,
                GL11.GL_QUADS, 256, glState);
    }

}
