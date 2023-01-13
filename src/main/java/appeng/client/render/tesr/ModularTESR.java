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

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

import appeng.block.orientation.BlockOrientation;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.client.render.renderable.Renderable;

@Environment(EnvType.CLIENT)
public class ModularTESR<T extends AEBaseBlockEntity> implements BlockEntityRenderer<T> {

    private final List<Renderable<? super T>> renderables;

    @SafeVarargs
    public ModularTESR(Renderable<? super T>... renderables) {
        this.renderables = ImmutableList.copyOf(renderables);
    }

    @Override
    public void render(T blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffers, int combinedLight,
            int combinedOverlay) {
        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);
        BlockOrientation blockOrientation = BlockOrientation.get(blockEntity);
        ms.mulPose(blockOrientation.getQuaternion());
        ms.translate(-0.5, -0.5, -0.5);
        for (Renderable<? super T> renderable : this.renderables) {
            renderable.renderBlockEntityAt(blockEntity, partialTicks, ms, buffers, combinedLight, combinedOverlay);
        }
        ms.popPose();
    }

}
