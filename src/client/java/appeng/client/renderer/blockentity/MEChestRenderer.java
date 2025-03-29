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

package appeng.client.renderer.blockentity;

import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.client.render.AERenderTypes;
import appeng.client.render.model.DriveModel;
import appeng.core.definitions.AEBlocks;
import appeng.thirdparty.fabric.ModelHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The block entity renderer for ME chests takes care of rendering the right model for the inserted cell, as well as the
 * LED.
 */
public class MEChestRenderer implements BlockEntityRenderer<MEChestBlockEntity> {

    private final ModelManager modelManager;

    private final ModelBlockRenderer blockRenderer;

    public MEChestRenderer(BlockEntityRendererProvider.Context context) {
        Minecraft client = Minecraft.getInstance();
        modelManager = client.getModelManager();
        blockRenderer = client.getBlockRenderer().getModelRenderer();
    }

    @Override
    public void render(MEChestBlockEntity chest, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
                       int combinedLight, int packedOverlay, Vec3 cameraPosition) {

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
        var driveModel = getDriveModel();
        if (driveModel == null) {
            return;
        }
        var cellModel = driveModel.getCellChassisModel(cellItem);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        var rotation = BlockOrientation.get(chest);
        poseStack.mulPose(rotation.getQuaternion());
        poseStack.translate(-0.5, -0.5, -0.5);

        // The models are created for the top-left slot of the drive model,
        // we need to move them into place for the slot on the ME chest
        poseStack.translate(5 / 16.0, 4 / 16.0, 0);

        var rotatedModelQuads = rotateQuadCullFaces(cellModel.quads(), rotation);
        List<BlockModelPart> parts = List.of(new SimpleModelWrapper(rotatedModelQuads, cellModel.useAmbientOcclusion(), cellModel.particleIcon(), cellModel.renderType()));

        // We "fake" the position here to make it use the light-value in front of the drive
        blockRenderer.tesselateBlock(level, parts, chest.getBlockState(), chest.getBlockPos(), poseStack, buffers::getBuffer,
                false, packedOverlay);

        VertexConsumer ledBuffer = buffers.getBuffer(AERenderTypes.STORAGE_CELL_LEDS);
        CellLedRenderer.renderLed(chest, 0, ledBuffer, poseStack, partialTicks);

        poseStack.popPose();
    }

    private DriveModel getDriveModel() {
        return (DriveModel) modelManager.getBlockModelShaper()
                .getBlockModel(AEBlocks.DRIVE.block().defaultBlockState());
    }

    /**
     * The actual vertex data will be transformed using the matrix stack, but the faces will not be correctly rotated so
     * the incorrect lighting data would be used to apply diffuse lighting and the lightmap texture.
     */
    private static QuadCollection rotateQuadCullFaces(QuadCollection quadCollection, BlockOrientation r) {
        var rotated = new QuadCollection.Builder();
        for (int cullFaceIdx = 0; cullFaceIdx <= ModelHelper.NULL_FACE_ID; cullFaceIdx++) {
            Direction cullFace = ModelHelper.faceFromIndex(cullFaceIdx);
            if (cullFace != null) {
                cullFace = r.resultingRotate(cullFace); // This fixes the incorrect lightmap position
            }

            var quads = quadCollection.getQuads(cullFace);
            for (var quad : quads) {
                var rotatedQuad = new BakedQuad(quad.vertices(), quad.tintIndex(), r.rotate(quad.direction()),
                        quad.sprite(), quad.shade(), quad.lightEmission());
                if (cullFace == null) {
                    rotated.addUnculledFace(rotatedQuad);
                } else {
                    rotated.addCulledFace(cullFace, rotatedQuad);
                }
            }
        }

        return rotated.build();
    }

}
