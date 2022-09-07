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
import com.mojang.math.Quaternion;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.client.render.FacingToRotation;
import appeng.tile.grindstone.CrankBlockEntity;

@OnlyIn(Dist.CLIENT)
public class CrankRenderer implements BlockEntityRenderer<CrankBlockEntity> {

    private final BlockRenderDispatcher blockRenderer;

    public CrankRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(CrankBlockEntity crank, float partialTick, PoseStack stack, MultiBufferSource buffers,
            int packedLight, int packedOverlay) {

        // Apply GL transformations relative to the center of the block: 1) TE rotation
        // and 2) crank rotation
        stack.pushPose();
        stack.translate(0.5, 0.5, 0.5);
        FacingToRotation.get(crank.getForward(), crank.getUp()).push(stack);
        stack.mulPose(new Quaternion(0, crank.getVisibleRotation(), 0, true));
        stack.translate(-0.5, -0.5, -0.5);

        var blockState = crank.getBlockState();
        var model = blockRenderer.getBlockModel(blockState);
        var buffer = buffers.getBuffer(RenderType.cutout());
        var pos = crank.getBlockPos();
        blockRenderer.getModelRenderer().tesselateWithAO(
                crank.getLevel(),
                model,
                blockState,
                pos,
                stack,
                buffer,
                false,
                RandomSource.create(),
                blockState.getSeed(pos),
                packedOverlay);
        stack.popPose();
    }
}
