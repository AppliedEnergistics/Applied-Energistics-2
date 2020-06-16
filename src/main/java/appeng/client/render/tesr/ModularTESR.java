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
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.client.render.FacingToRotation;
import appeng.client.render.renderable.Renderable;
import appeng.tile.AEBaseTile;

@OnlyIn(Dist.CLIENT)
public class ModularTESR<T extends AEBaseTile> extends TileEntityRenderer<T> {

    private final List<Renderable<? super T>> renderables;

    @SafeVarargs
    public ModularTESR(TileEntityRendererDispatcher rendererDispatcherIn, Renderable<? super T>... renderables) {
        super(rendererDispatcherIn);
        this.renderables = ImmutableList.copyOf(renderables);
    }

    @Override
    public void render(T te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffers, int combinedLight,
            int combinedOverlay) {
        ms.push();
        ms.translate(0.5, 0.5, 0.5);
        FacingToRotation.get(te.getForward(), te.getUp()).push(ms);
        ms.translate(-0.5, -0.5, -0.5);
        for (Renderable<? super T> renderable : this.renderables) {
            renderable.renderTileEntityAt(te, partialTicks, ms, buffers, combinedLight, combinedOverlay);
        }
        ms.pop();
    }

}