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

package appeng.client.render.tesr;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.blockentity.storage.SkyStoneTankBlockEntity;

@Environment(EnvType.CLIENT)
public final class SkyStoneTankBlockEntityRenderer implements BlockEntityRenderer<SkyStoneTankBlockEntity> {

    public SkyStoneTankBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(SkyStoneTankBlockEntity tank, float tickDelta, PoseStack ms, MultiBufferSource vertexConsumers,
            int light, int overlay) {
        if (!tank.getStorage().variant.isBlank() && tank.getStorage().amount > 0) {

            /*
             * 
             * // Uncomment to allow the liquid to rotate with the tank ms.pushPose(); ms.translate(0.5, 0.5, 0.5);
             * FacingToRotation.get(tank.getForward(), tank.getUp()).push(ms); ms.translate(-0.5, -0.5, -0.5);
             */

            drawFluidInTank(tank, ms, vertexConsumers, tank.getStorage().variant,
                    (float) tank.getStorage().amount / tank.getStorage().getCapacity());

            // ms.popPose();
        }
    }

    private static final float TANK_W = 1 / 16f + 0.001f; // avoiding Z-fighting
    public static final int FULL_LIGHT = 0x00F0_00F0;

    public static void drawFluidInTank(BlockEntity be, PoseStack ms, MultiBufferSource vcp, FluidVariant fluid,
            float fill) {
        drawFluidInTank(be.getLevel(), be.getBlockPos(), ms, vcp, fluid, fill);
    }

    public static void drawFluidInTank(Level level, BlockPos pos, PoseStack ps, MultiBufferSource mbs,
            FluidVariant fluid, float fill) {
        // From Modern Industrialization
        VertexConsumer vc = mbs.getBuffer(RenderType.translucent());
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);

        int color = FluidVariantRendering.getColor(fluid, level, pos);

        float r = ((color >> 16) & 255) / 256f;
        float g = ((color >> 8) & 255) / 256f;
        float b = (color & 255) / 256f;

        fill = TANK_W + (1 - 2 * TANK_W) * Math.min(1, Math.max(fill, 0));

        // Top and bottom positions of the fluid inside the tank
        float topHeight = fill;
        float bottomHeight = TANK_W;

        // Render gas from top to bottom
        if (FluidVariantAttributes.isLighterThanAir(fluid)) {
            topHeight = 1 - TANK_W;
            bottomHeight = 1 - fill;
        }

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        for (Direction direction : Direction.values()) {
            QuadEmitter emitter = renderer.meshBuilder().getEmitter();

            if (direction.getAxis().isVertical()) {
                emitter.square(direction, TANK_W, TANK_W, 1 - TANK_W, 1 - TANK_W,
                        direction == Direction.UP ? 1 - topHeight : bottomHeight);
            } else {
                emitter.square(direction, TANK_W, bottomHeight, 1 - TANK_W, topHeight, TANK_W);
            }

            emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            vc.putBulkData(ps.last(), emitter.toBakedQuad(0, sprite, false), r, g, b, FULL_LIGHT,
                    OverlayTexture.NO_OVERLAY);
        }
    }

}
