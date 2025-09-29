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

package appeng.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import org.joml.Quaternionf;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Mth;

import appeng.core.definitions.AEBlocks;
import appeng.entity.TinyTNTPrimedEntity;

public class TinyTNTPrimedRenderer extends EntityRenderer<TinyTNTPrimedEntity, TntRenderState> {
    public TinyTNTPrimedRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.25F;
    }

    @Override
    public TntRenderState createRenderState() {
        return new TntRenderState();
    }

    @Override
    public void extractRenderState(TinyTNTPrimedEntity p_entity, TntRenderState reusedState, float partialTick) {
        super.extractRenderState(p_entity, reusedState, partialTick);
        reusedState.fuseRemainingInTicks = p_entity.getFuse();
    }

    @Override
    public void submit(TntRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0, 0.5F, 0);
        float f2;

        if (renderState.fuseRemainingInTicks - renderState.partialTick + 1.0F < 10.0F) {
            f2 = 1.0F - (renderState.fuseRemainingInTicks - renderState.partialTick + 1.0F) / 10.0F;

            if (f2 < 0.0F) {
                f2 = 0.0F;
            }

            if (f2 > 1.0F) {
                f2 = 1.0F;
            }

            f2 *= f2;
            f2 *= f2;
            final float f3 = 1.0F + f2 * 0.3F;
            poseStack.scale(f3, f3, f3);
        }

        poseStack.mulPose(new Quaternionf().rotationY(Mth.DEG_TO_RAD * -90.0F));
        poseStack.translate(-0.5D, -0.5D, 0.5D);
        poseStack.mulPose(new Quaternionf().rotationY(Mth.DEG_TO_RAD * 90.0F));
        TntMinecartRenderer.submitWhiteSolidBlock(
                AEBlocks.TINY_TNT.block().defaultBlockState(),
                poseStack,
                nodes,
                renderState.lightCoords,
                renderState.fuseRemainingInTicks / 5 % 2 == 0,
                renderState.outlineColor);
        poseStack.popPose();

        super.submit(renderState, poseStack, nodes, cameraRenderState);
    }

}
