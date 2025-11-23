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

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import appeng.blockentity.storage.SkyStoneTankBlockEntity;
import appeng.client.render.CubeBuilder;

public final class SkyStoneTankRenderer
        implements BlockEntityRenderer<SkyStoneTankBlockEntity, SkyStoneTankRenderState> {

    public SkyStoneTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public SkyStoneTankRenderState createRenderState() {
        return new SkyStoneTankRenderState();
    }

    @Override
    public void extractRenderState(SkyStoneTankBlockEntity be, SkyStoneTankRenderState state, float partialTicks,
            Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPos, crumblingOverlay);

        var fluidHandler = be.getFluidHandler();
        var resource = fluidHandler.getResource(0);
        var capacity = fluidHandler.getCapacityAsLong(0, resource);
        var amount = fluidHandler.getAmountAsLong(0);
        if (resource.isEmpty() || capacity <= 0 || amount <= 0) {
            state.fill = 0;
            state.sprite = null;
            return;
        }

        var fluidStack = resource.toStack(1);

        state.fill = (float) amount / capacity;
        var renderProps = IClientFluidTypeExtensions.of(resource.getFluid());
        state.sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(renderProps.getStillTexture(fluidStack));
        state.color = renderProps.getTintColor(fluidStack);
        state.lighterThanAir = resource.getFluidType().isLighterThanAir();
    }

    @Override
    public void submit(SkyStoneTankRenderState state, PoseStack poseStack, SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {

        if (state.sprite == null) {
            return;
        }

        /*
         *
         * // Uncomment to allow the liquid to rotate with the tank ms.pushPose(); ms.translate(0.5, 0.5, // 0.5);
         * FacingToRotation.get(tank.getForward(), tank.getUp()).push(ms); ms.translate(-0.5, -0.5, -0.5);
         */

        // From Modern Industrialization
        nodes.submitCustomGeometry(poseStack, RenderType.translucentMovingBlock(), (pose, consumer) -> {
            var fill = state.fill;
            var color = state.color;

            float r = ((color >> 16) & 255) / 256f;
            float g = ((color >> 8) & 255) / 256f;
            float b = (color & 255) / 256f;

            var fillY = Mth.lerp(Mth.clamp(fill, 0, 1), TANK_W, 1 - TANK_W);

            // Top and bottom positions of the fluid inside the tank
            float topHeight = fillY;
            float bottomHeight = TANK_W;

            // Render gas from top to bottom
            if (state.lighterThanAir) {
                topHeight = 1 - TANK_W;
                bottomHeight = 1 - fillY;
            }

            var builder = new CubeBuilder(bakedQuad -> {
                consumer.putBulkData(pose, bakedQuad, r, g, b, 1.0f, state.lightCoords, OverlayTexture.NO_OVERLAY);
            });
            builder.setTexture(state.sprite);

            var x1 = TANK_W * 16;
            var z1 = TANK_W * 16;
            var x2 = (1 - TANK_W) * 16;
            var z2 = (1 - TANK_W) * 16;
            var y1 = bottomHeight * 16;
            var y2 = topHeight * 16;
            builder.addCube(x1, y1, z1, x2, y2, z2);
        });
    }

    private static final float TANK_W = 1 / 16f + 0.001f; // avoiding Z-fighting

}
