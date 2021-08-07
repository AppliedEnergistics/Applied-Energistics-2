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

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

import appeng.api.parts.IPart;

public class CableBusTESR implements BlockEntityRenderer<CableBusBlockEntity> {

    public CableBusTESR(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CableBusBlockEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffers,
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

    @Override
    public int getViewDistance() {
        return 900;
    }

}
