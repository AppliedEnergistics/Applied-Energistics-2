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

import com.mojang.blaze3d.vertex.PoseStack;

import org.joml.Quaternionf;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.misc.CrankBlockEntity;
import appeng.core.AppEng;

@OnlyIn(Dist.CLIENT)
public class CrankRenderer implements BlockEntityRenderer<CrankBlockEntity> {

    public static final ModelResourceLocation BASE_MODEL = ModelResourceLocation
            .standalone(AppEng.makeId("block/crank_base"));
    public static final ModelResourceLocation HANDLE_MODEL = ModelResourceLocation
            .standalone(AppEng.makeId("block/crank_handle"));

    private final BlockRenderDispatcher blockRenderer;

    private final ModelManager modelManager;

    public CrankRenderer(BlockEntityRendererProvider.Context context) {
        this.modelManager = context.getBlockRenderDispatcher().getBlockModelShaper().getModelManager();
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(CrankBlockEntity crank, float partialTick, PoseStack stack, MultiBufferSource buffers,
            int packedLight, int packedOverlay) {

        var baseModel = modelManager.getModel(BASE_MODEL);
        var handleModel = modelManager.getModel(HANDLE_MODEL);

        var blockState = crank.getBlockState();
        var buffer = buffers.getBuffer(RenderType.cutout());
        var pos = crank.getBlockPos();

        // Apply GL transformations relative to the center of the block:
        // 1) Block orientation. The cranks top faces NORTH by default
        // and 2) crank rotation
        stack.pushPose();
        stack.translate(0.5, 0.5, 0.5);
        stack.mulPose(BlockOrientation.get(crank).getQuaternion());
        stack.translate(-0.5, -0.5, -0.5);

        // Render the base model followed by the actual crank model
        blockRenderer.getModelRenderer().tesselateWithAO(
                crank.getLevel(),
                baseModel,
                blockState,
                pos,
                stack,
                buffer,
                false,
                RandomSource.create(),
                blockState.getSeed(pos),
                packedOverlay);

        // The unrotated cranks orientation is towards NORTH (negative Z axis)
        stack.translate(0.5, 0.5, 0.5);
        stack.mulPose(new Quaternionf().rotationZ(-Mth.DEG_TO_RAD * crank.getVisibleRotation()));
        stack.translate(-0.5, -0.5, -0.5);

        blockRenderer.getModelRenderer().tesselateWithAO(
                crank.getLevel(),
                handleModel,
                blockState,
                pos,
                stack,
                buffer,
                false,
                RandomSource.create(),
                blockState.getSeed(pos),
                packedOverlay);
        stack.popPose();
    }

}
