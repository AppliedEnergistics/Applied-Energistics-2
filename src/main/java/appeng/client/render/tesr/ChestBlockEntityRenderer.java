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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.storage.ChestBlockEntity;
import appeng.client.render.BakedModelUnwrapper;
import appeng.client.render.model.DriveBakedModel;
import appeng.core.definitions.AEBlocks;

/**
 * The block entity renderer for ME chests takes care of rendering the right model for the inserted cell, as well as the
 * LED.
 */
public class ChestBlockEntityRenderer implements BlockEntityRenderer<ChestBlockEntity> {

    private final ModelManager modelManager;

    private final ModelBlockRenderer blockRenderer;

    public ChestBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        Minecraft client = Minecraft.getInstance();
        modelManager = client.getModelManager();
        blockRenderer = client.getBlockRenderer().getModelRenderer();
    }

    @Override
    public void render(ChestBlockEntity chest, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
            int combinedLight, int combinedOverlay) {

        Level level = chest.getLevel();
        if (level == null) {
            return;
        }

        var cellItem = chest.getCellItem(0);
        if (cellItem == null) {
            return; // No cell inserted into chest
        }

        // Try to get the right cell chassis model from the drive model since it already
        // loads them all
        DriveBakedModel driveModel = getDriveModel();
        if (driveModel == null) {
            return;
        }
        BakedModel cellModel = driveModel.getCellChassisModel(cellItem);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        var rotation = BlockOrientation.get(chest);
        poseStack.mulPose(rotation.getQuaternion());
        poseStack.translate(-0.5, -0.5, -0.5);

        // The models are created for the top-left slot of the drive model,
        // we need to move them into place for the slot on the ME chest
        poseStack.translate(5 / 16.0, 4 / 16.0, 0);

        // Render the cell model as-if it was a block model
        VertexConsumer buffer = buffers.getBuffer(RenderType.cutout());
        // We "fake" the position here to make it use the light-value in front of the
        // drive
        FaceRotatingModel rotatedModel = new FaceRotatingModel(cellModel, rotation);
        blockRenderer.tesselateBlock(level, rotatedModel, chest.getBlockState(), chest.getBlockPos(), poseStack, buffer,
                false,
                RandomSource.create(), 0L, combinedOverlay);

        VertexConsumer ledBuffer = buffers.getBuffer(CellLedRenderer.RENDER_LAYER);
        CellLedRenderer.renderLed(chest, 0, ledBuffer, poseStack, partialTicks);

        poseStack.popPose();
    }

    private DriveBakedModel getDriveModel() {
        BakedModel driveModel = modelManager.getBlockModelShaper()
                .getBlockModel(AEBlocks.DRIVE.block().defaultBlockState());
        return BakedModelUnwrapper.unwrap(driveModel, DriveBakedModel.class);
    }

    /**
     * The actual vertex data will be transformed using the matrix stack, but the faces will not be correctly rotated so
     * the incorrect lighting data would be used to apply diffuse lighting and the lightmap texture.
     */
    private static class FaceRotatingModel extends ForwardingBakedModel {
        private final BlockOrientation r;

        protected FaceRotatingModel(BakedModel base, BlockOrientation r) {
            this.wrapped = base;
            this.r = r;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
            if (side != null) {
                side = r.resultingRotate(side); // This fixes the incorrect lightmap position
            }
            List<BakedQuad> quads = new ArrayList<>(super.getQuads(state, side, rand));

            for (int i = 0; i < quads.size(); i++) {
                BakedQuad quad = quads.get(i);
                quads.set(i, new BakedQuad(quad.getVertices(), quad.getTintIndex(), r.rotate(quad.getDirection()),
                        quad.getSprite(), quad.isShade()));
            }
            return quads;
        }
    }

}
