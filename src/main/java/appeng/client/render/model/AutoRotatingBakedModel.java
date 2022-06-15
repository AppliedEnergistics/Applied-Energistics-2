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

package appeng.client.render.model;

import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import appeng.client.render.cablebus.QuadRotator;

@Environment(EnvType.CLIENT)
public class AutoRotatingBakedModel extends ForwardingBakedModel implements FabricBakedModel {

    public AutoRotatingBakedModel(BakedModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    public BakedModel getWrapped() {
        return wrapped;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos,
            Supplier<RandomSource> randomSupplier, RenderContext context) {
        RenderContext.QuadTransform transform = getTransform(blockView, pos);

        if (transform != null) {
            context.pushTransform(transform);
        }

        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);

        if (transform != null) {
            context.popTransform();
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        super.emitItemQuads(stack, randomSupplier, context);
    }

    private RenderContext.QuadTransform getTransform(BlockAndTintGetter view, BlockPos pos) {
        if (!(view instanceof RenderAttachedBlockView renderBlockView)) {
            return null;
        }

        Object data = renderBlockView.getBlockEntityRenderAttachment(pos);
        if (!(data instanceof AEModelData aeModelData)) {
            return null;
        }

        RenderContext.QuadTransform transform = QuadRotator.get(aeModelData.getForward(), aeModelData.getUp());
        if (transform == QuadRotator.NULL_TRANSFORM) {
            return null;
        }
        return transform;
    }

}
