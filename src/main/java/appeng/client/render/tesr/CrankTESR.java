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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.client.render.FacingToRotation;
import appeng.tile.grindstone.CrankBlockEntity;

/**
 * This FastTESR only handles the animated model of the turning crank. When the crank is at rest, it is rendered using a
 * normal model.
 */
@OnlyIn(Dist.CLIENT)
public class CrankTESR implements BlockEntityRenderer<CrankBlockEntity> {

    public CrankTESR(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CrankBlockEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffers,
                       int combinedLightIn, int combinedOverlayIn) {

        // Apply GL transformations relative to the center of the block: 1) TE rotation
        // and 2) crank rotation
        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);
        FacingToRotation.get(te.getForward(), te.getUp()).push(ms);
        ms.mulPose(new Quaternion(0, te.getVisibleRotation(), 0, true));
        ms.translate(-0.5, -0.5, -0.5);

        BlockState blockState = te.getBlockState();
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(blockState);
        VertexConsumer buffer = buffers.getBuffer(Sheets.cutoutBlockSheet());
        dispatcher.getModelRenderer().renderModel(ms.last(), buffer, null, model, 1, 1, 1,
                combinedLightIn, combinedOverlayIn);
        ms.popPose();

    }

}
