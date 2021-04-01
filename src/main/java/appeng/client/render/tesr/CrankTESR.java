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

import appeng.tile.grindstone.CrankTileEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Quaternion;
import appeng.client.render.FacingToRotation;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

/**
 * This FastTESR only handles the animated model of the turning crank. When the crank is at rest, it is rendered using a
 * normal model.
 */
@Environment(EnvType.CLIENT)
public class CrankTESR extends TileEntityRenderer<CrankTileEntity> {

    public CrankTESR(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(CrankTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffers,
                       int combinedLightIn, int combinedOverlayIn) {

        // Apply GL transformations relative to the center of the block: 1) TE rotation
        // and 2) crank rotation
        ms.push();
        ms.translate(0.5, 0.5, 0.5);
        FacingToRotation.get(te.getForward(), te.getUp()).push(ms);
        ms.rotate(new Quaternion(0, te.getVisibleRotation(), 0, true));
        ms.translate(-0.5, -0.5, -0.5);

        BlockState blockState = te.getBlockState();
        BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        IBakedModel model = dispatcher.getModelForState(blockState);
        IVertexBuilder buffer = buffers.getBuffer(Atlases.getTranslucentCullBlockType());
        dispatcher.getBlockModelRenderer().renderModelBrightnessColor(ms.getLast(), buffer, null, model, 1, 1, 1, combinedLightIn,
                combinedOverlayIn);
        ms.pop();

    }

}
