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

package appeng.client.render.tesr;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Quaternion;

import appeng.client.render.FacingToRotation;
import appeng.tile.grindstone.CrankBlockEntity;

/**
 * This FastTESR only handles the animated model of the turning crank. When the crank is at rest, it is rendered using a
 * normal model.
 */
@Environment(EnvType.CLIENT)
public class CrankTESR implements BlockEntityRenderer<CrankBlockEntity> {

    public CrankTESR(BlockEntityRendererFactory.Context rendererDispatcherIn) {
    }

    @Override
    public void render(CrankBlockEntity te, float partialTicks, MatrixStack ms, VertexConsumerProvider buffers,
            int combinedLightIn, int combinedOverlayIn) {

        // Apply GL transformations relative to the center of the block: 1) TE rotation
        // and 2) crank rotation
        ms.push();
        ms.translate(0.5, 0.5, 0.5);
        FacingToRotation.get(te.getForward(), te.getUp()).push(ms);
        ms.multiply(new Quaternion(0, te.getVisibleRotation(), 0, true));
        ms.translate(-0.5, -0.5, -0.5);

        BlockState blockState = te.getCachedState();
        BlockRenderManager dispatcher = MinecraftClient.getInstance().getBlockRenderManager();
        BakedModel model = dispatcher.getModel(blockState);
        VertexConsumer buffer = buffers.getBuffer(TexturedRenderLayers.getEntityTranslucentCull());
        dispatcher.getModelRenderer().render(ms.peek(), buffer, null, model, 1, 1, 1, combinedLightIn,
                combinedOverlayIn);
        ms.pop();

    }

}
