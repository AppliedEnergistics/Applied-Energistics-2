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


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;
import java.util.function.Function;


public class ItemRenderable<T extends TileEntity> implements Renderable<T> {

    private final Function<T, Pair<ItemStack, Matrix4f>> f;

    public ItemRenderable(Function<T, Pair<ItemStack, Matrix4f>> f) {
        this.f = f;
    }

    @Override
    public void renderTileEntityAt(T te, double x, double y, double z, float partialTicks, int destroyStage) {
        Pair<ItemStack, Matrix4f> pair = this.f.apply(te);
        if (pair != null && pair.getLeft() != null) {
            GlStateManager.pushMatrix();
            if (pair.getRight() != null) {
                FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
                pair.getRight().store(matrix);
                matrix.flip();
                GlStateManager.multMatrix(matrix);
            }
            Minecraft.getMinecraft().getRenderItem().renderItem(pair.getLeft(), TransformType.GROUND);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void renderTileEntityFast(T te, double x, double y, double z, float partialTicks, int destroyStage, BufferBuilder buffer) {

    }

}
