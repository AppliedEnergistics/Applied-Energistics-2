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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelDataMap;

import appeng.client.render.FacingToRotation;
import appeng.client.render.model.SkyCompassBakedModel;
import appeng.tile.misc.SkyCompassTileEntity;

@OnlyIn(Dist.CLIENT)
public class SkyCompassTESR extends TileEntityRenderer<SkyCompassTileEntity> {

    private static BlockRendererDispatcher blockRenderer;

    public SkyCompassTESR(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(SkyCompassTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffers,
            int combinedLightIn, int combinedOverlayIn) {

        if (blockRenderer == null) {
            blockRenderer = Minecraft.getInstance().getBlockRenderer();
        }

        IVertexBuilder buffer = buffers.getBuffer(Atlases.cutoutBlockSheet());

        BlockState blockState = te.getBlockState();
        IBakedModel model = blockRenderer.getBlockModelShaper().getBlockModel(blockState);

        // FIXME: Rotation was previously handled by an auto rotating model I think, but
        // FIXME: Should be handled using matrices instead
        Direction forward = te.getForward();
        Direction up = te.getUp();
        // This ensures the needle isn't flipped by the model rotator. Since the model
        // is symmetrical, this should
        // not affect the appearance
        if (forward == Direction.UP || forward == Direction.DOWN) {
            up = Direction.NORTH;
        }
        // Flip forward/up for rendering, the base model is facing up without any
        // rotation
        ms.pushPose();
        ms.translate(0.5D, 0.5D, 0.5D);
        FacingToRotation.get(up, forward).push(ms);
        ms.translate(-0.5D, -0.5D, -0.5D);

        ModelDataMap modelData = new ModelDataMap.Builder().withInitial(SkyCompassBakedModel.ROTATION, getRotation(te))
                .build();

        blockRenderer.getModelRenderer().renderModel(ms.last(), buffer, null, model, 1, 1, 1, combinedLightIn,
                combinedOverlayIn, modelData);
        ms.popPose();

    }

    private static float getRotation(SkyCompassTileEntity skyCompass) {
        float rotation;

        if (skyCompass.getForward() == Direction.UP || skyCompass.getForward() == Direction.DOWN) {
            rotation = SkyCompassBakedModel.getAnimatedRotation(skyCompass.getBlockPos(), false);
        } else {
            rotation = SkyCompassBakedModel.getAnimatedRotation(null, false);
        }

        if (skyCompass.getForward() == Direction.DOWN) {
            rotation = flipidiy(rotation);
        }

        return rotation;
    }

    private static float flipidiy(float rad) {
        float x = (float) Math.cos(rad);
        float y = (float) Math.sin(rad);
        return (float) Math.atan2(-y, x);
    }
}
