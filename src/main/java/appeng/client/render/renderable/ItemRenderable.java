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

package appeng.client.render.renderable;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemRenderable<T extends BlockEntity> implements Renderable<T> {

    private final Function<T, Pair<ItemStack, Transformation>> f;

    public ItemRenderable(Function<T, Pair<ItemStack, Transformation>> f) {
        this.f = f;
    }

    @Override
    public void renderBlockEntityAt(T te, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffers, int combinedLight, int combinedOverlay) {
        Pair<ItemStack, Transformation> pair = this.f.apply(te);
        if (pair != null && pair.getLeft() != null) {
            poseStack.pushPose();
            if (pair.getRight() != null) {
                // TODO FABRIC 117 Verify this is correct
                poseStack.mulPoseMatrix(pair.getRight().getMatrix());
            }
            Minecraft.getInstance().getItemRenderer().renderStatic(pair.getLeft(),
                    TransformType.GROUND, combinedLight, combinedOverlay, poseStack, buffers, 0);
            poseStack.popPose();
        }
    }

}
