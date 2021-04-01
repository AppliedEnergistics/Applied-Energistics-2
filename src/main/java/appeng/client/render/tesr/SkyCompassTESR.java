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

import appeng.tile.misc.SkyCompassTileEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import appeng.client.render.BakedModelUnwrapper;
import appeng.client.render.FacingToRotation;
import appeng.client.render.model.SkyCompassBakedModel;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

@Environment(EnvType.CLIENT)
public class SkyCompassTESR extends TileEntityRenderer<SkyCompassTileEntity> {

    private static BlockRendererDispatcher blockRenderer;

    public SkyCompassTESR(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(SkyCompassTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffers,
                       int combinedLightIn, int combinedOverlayIn) {
        if (blockRenderer == null) {
            blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        }

        BlockModelRenderer modelRenderer = blockRenderer.getBlockModelRenderer();

        BlockState blockState = te.getBlockState();
        IBakedModel model = blockRenderer.getBlockModelShapes().getModel(blockState);
        SkyCompassBakedModel skyCompassModel = BakedModelUnwrapper.unwrap(model, SkyCompassBakedModel.class);
        if (skyCompassModel == null) {
            return;
        }

        IBakedModel pointerModel = skyCompassModel.getPointer();

        Direction forward = te.getForward();
        Direction up = te.getUp();
        // This ensures the needle isn't flipped by the model rotator. Since the model
        // is symmetrical, this should
        // not affect the appearance
        if (forward == Direction.field_11036 || forward == Direction.field_11033) {
            up = Direction.field_11043;
        }
        ms.push();

        IVertexBuilder buffer = buffers.getBuffer(RenderType.getSolid());

        float rotation = getRotation(te);
        ms.translate(0.5D, 0.5D, 0.5D);
        // Flip forward/up for rendering, the base model
        // is facing up without any rotation
        FacingToRotation.get(up, forward).push(ms);
        ms.rotate(new Quaternion(0, rotation, 0, false));
        ms.translate(-0.5D, -0.5D, -0.5D);

        modelRenderer.renderModelBrightnessColor(ms.getLast(), buffer, null, pointerModel, 1, 1, 1, combinedLightIn, combinedOverlayIn);
        ms.pop();

    }

    // FIXME FABRIC This needs to go to the tile entity (?)
    private static float getRotation(SkyCompassTileEntity skyCompass) {
        float rotation = 0;

        if (skyCompass.getForward() == Direction.field_11036 || skyCompass.getForward() == Direction.field_11033) {
            rotation = SkyCompassBakedModel.getAnimatedRotation(skyCompass.getPos(), false);
        } else {
            rotation = SkyCompassBakedModel.getAnimatedRotation(null, false);
        }

        if (skyCompass.getForward() == Direction.field_11033) {
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
