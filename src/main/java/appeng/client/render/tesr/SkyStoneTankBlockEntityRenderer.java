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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import appeng.blockentity.storage.SkyStoneTankBlockEntity;
import appeng.client.render.cablebus.CubeBuilder;

public final class SkyStoneTankBlockEntityRenderer implements BlockEntityRenderer<SkyStoneTankBlockEntity> {

    public SkyStoneTankBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SkyStoneTankBlockEntity tank, float tickDelta, PoseStack ms, MultiBufferSource vertexConsumers,
            int light, int overlay) {
        if (!tank.getStorage().getFluid().isEmpty()) {

            /*
             * 
             * // Uncomment to allow the liquid to rotate with the tank ms.pushPose(); ms.translate(0.5, 0.5, 0.5);
             * FacingToRotation.get(tank.getForward(), tank.getUp()).push(ms); ms.translate(-0.5, -0.5, -0.5);
             */

            drawFluidInTank(tank, ms, vertexConsumers, tank.getStorage().getFluid(),
                    (float) tank.getStorage().getFluid().getAmount() / tank.getStorage().getCapacity());

            // ms.popPose();
        }
    }

    private static final float TANK_W = 1 / 16f + 0.001f; // avoiding Z-fighting
    public static final int FULL_LIGHT = 0x00F0_00F0;

    public static void drawFluidInTank(BlockEntity be, PoseStack ms, MultiBufferSource vcp, FluidStack fluid,
            float fill) {
        drawFluidInTank(be.getLevel(), be.getBlockPos(), ms, vcp, fluid, fill);
    }

    public static void drawFluidInTank(Level level, BlockPos pos, PoseStack ps, MultiBufferSource mbs,
            FluidStack fluid, float fill) {
        // From Modern Industrialization
        VertexConsumer vc = mbs.getBuffer(RenderType.translucentMovingBlock());
        var renderProps = IClientFluidTypeExtensions.of(fluid.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(renderProps.getStillTexture(fluid));

        int color = renderProps.getTintColor(fluid);

        float r = ((color >> 16) & 255) / 256f;
        float g = ((color >> 8) & 255) / 256f;
        float b = (color & 255) / 256f;

        var fillY = Mth.lerp(Mth.clamp(fill, 0, 1), TANK_W, 1 - TANK_W);

        // Top and bottom positions of the fluid inside the tank
        float topHeight = fillY;
        float bottomHeight = TANK_W;

        // Render gas from top to bottom
        var attributes = fluid.getFluid().getFluidType();
        if (attributes.isLighterThanAir()) {
            topHeight = 1 - TANK_W;
            bottomHeight = 1 - fillY;
        }

        var builder = new CubeBuilder();
        builder.setTexture(sprite);

        var x1 = TANK_W * 16;
        var z1 = TANK_W * 16;
        var x2 = (1 - TANK_W) * 16;
        var z2 = (1 - TANK_W) * 16;
        var y1 = bottomHeight * 16;
        var y2 = topHeight * 16;
        builder.addCube(x1, y1, z1, x2, y2, z2);

        for (var bakedQuad : builder.getOutput()) {
            vc.putBulkData(ps.last(), bakedQuad, r, g, b, FULL_LIGHT,
                    OverlayTexture.NO_OVERLAY);
        }

    }

}
