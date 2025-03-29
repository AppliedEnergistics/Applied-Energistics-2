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

package appeng.client.renderer.blockentity;

import appeng.blockentity.networking.CableBusBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

import appeng.api.parts.IPart;
import net.minecraft.world.phys.Vec3;

/**
 * Renders dynamic aspects of parts attached to a cable bus.
 */
public class CableBusRenderer implements BlockEntityRenderer<CableBusBlockEntity> {

    public CableBusRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CableBusBlockEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffers,
            int combinedLightIn, int combinedOverlayIn, Vec3 cameraPosition) {
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
