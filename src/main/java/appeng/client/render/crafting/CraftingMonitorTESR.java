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

import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.util.math.Direction;
import net.fabricmc.api.EnvType;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.render.TesrRenderHelper;
import appeng.tile.crafting.CraftingMonitorBlockEntity;

/**
 * Renders the item currently being crafted
 */
@Environment(EnvType.CLIENT)
public class CraftingMonitorTESR extends BlockEntityRenderer<CraftingMonitorBlockEntity> {

    public CraftingMonitorTESR(BlockEntityRenderDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(CraftingMonitorBlockEntity te, float partialTicks, MatrixStack matrixStack,
                       VertexConsumerProvider buffers, int combinedLight, int combinedOverlay) {

        Direction facing = te.getForward();

        IAEItemStack jobProgress = te.getJobProgress();

        if (jobProgress != null) {
            matrixStack.push();
            matrixStack.translate(0.5, 0.5, 0.5); // Move to the center of the block

            TesrRenderHelper.rotateToFace(matrixStack, facing, (byte) 0);
            matrixStack.translate(0, 0.08, 0.5);
            TesrRenderHelper.renderItem2dWithAmount(matrixStack, buffers, jobProgress, 0.3f, -0.18f, 15728880,
                    combinedOverlay);

            matrixStack.pop();
        }
    }
}
