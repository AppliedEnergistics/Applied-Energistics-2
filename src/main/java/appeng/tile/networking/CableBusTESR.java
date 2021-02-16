/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.tile.networking;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

import appeng.api.parts.IPart;

public class CableBusTESR implements BlockEntityRenderer<CableBusBlockEntity> {

    public CableBusTESR(BlockEntityRendererFactory.Context rendererDispatcherIn) {
    }

    @Override
    public void render(CableBusBlockEntity te, float partialTicks, MatrixStack ms, VertexConsumerProvider buffers,
            int combinedLightIn, int combinedOverlayIn) {
        if (!te.getCableBus().isRequiresDynamicRender()) {
            return;
        }

        for (Direction facing : Direction.values()) {
            IPart part = te.getPart(facing);
            if (part != null && part.requireDynamicRender()) {
                part.renderDynamic(partialTicks, ms, buffers, combinedLightIn, combinedOverlayIn);
            }
        }
    }
}
