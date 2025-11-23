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

import java.util.List;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.quad.QuadTransforms;

import appeng.api.client.StorageCellModels;
import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.client.render.AERenderTypes;
import appeng.thirdparty.fabric.ModelHelper;

/**
 * The block entity renderer for ME chests takes care of rendering the right model for the inserted cell, as well as the
 * LED.
 */
public class MEChestRenderer implements BlockEntityRenderer<MEChestBlockEntity, MEChestRenderState> {

    private final ModelManager modelManager;

    public MEChestRenderer(BlockEntityRendererProvider.Context context) {
        Minecraft client = Minecraft.getInstance();
        modelManager = client.getModelManager();
    }

    @Override
    public MEChestRenderState createRenderState() {
        return new MEChestRenderState();
    }

    @Override
    public void extractRenderState(MEChestBlockEntity be, MEChestRenderState state, float partialTicks, Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPos, crumblingOverlay);

        // Calculate the lightlevel in front of the drive for lighting the exposed cell model.
        if (be.getLevel() != null) {
            var frontPos = be.getBlockPos().relative(be.getFront());
            state.frontLightCoords = LevelRenderer.getLightColor(be.getLevel(), frontPos);
        } else {
            state.frontLightCoords = LightTexture.FULL_BRIGHT;
        }

        var blockOrientation = BlockOrientation.get(be);
        state.extract(blockOrientation, be, partialTicks);

        Level level = be.getLevel();
        if (level == null) {
            return;
        }

        var cellItem = be.getCellItem(0);
        if (cellItem == null) {
            return; // No cell inserted into chest
        }

        // Try to get the right cell chassis model from the drive model since it already
        // loads them all
        var cellModelKey = StorageCellModels.standaloneModel(cellItem);
        if (cellModelKey == null) {
            cellModelKey = StorageCellModels.getDefaultStandaloneModel();
        }
        state.cellModel = modelManager.getStandaloneModel(cellModelKey);
    }

    @Override
    public void submit(MEChestRenderState state, PoseStack poseStack, SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {

        var cellModel = state.cellModel;
        if (cellModel == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(state.blockOrientation.getQuaternion());
        poseStack.translate(-0.5, -0.5, -0.5);

        // The models are created for the top-left slot of the drive model,
        // we need to move them into place for the slot on the ME chest
        poseStack.translate(5 / 16.0, 4 / 16.0, 0);

        var rotatedModelQuads = rotateQuadCullFaces(cellModel::getQuads, state.blockOrientation);
        var chunkSectionLayer = cellModel.getRenderType(state.blockState);
        var renderType = RenderTypeHelper.getEntityRenderType(chunkSectionLayer);
        nodes.submitBlockModel(
                poseStack,
                renderType,
                new SingleVariant(new SimpleModelWrapper(rotatedModelQuads, cellModel.useAmbientOcclusion(),
                        cellModel.particleIcon(), chunkSectionLayer)),
                1, 1, 1,
                state.frontLightCoords,
                OverlayTexture.NO_OVERLAY,
                0);

        nodes.submitCustomGeometry(
                poseStack,
                AERenderTypes.STORAGE_CELL_LEDS,
                (pose, consumer) -> CellLedRenderer.renderLed(state.cellColors[0], consumer, pose));

        poseStack.popPose();
    }

    /**
     * The actual vertex data will be transformed using the matrix stack, but the faces will not be correctly rotated so
     * the incorrect lighting data would be used to apply diffuse lighting and the lightmap texture.
     */
    private static QuadCollection rotateQuadCullFaces(Function<Direction, List<BakedQuad>> quadCollection,
            BlockOrientation r) {
        var rotated = new QuadCollection.Builder();
        for (int cullFaceIdx = 0; cullFaceIdx <= ModelHelper.NULL_FACE_ID; cullFaceIdx++) {
            Direction cullFace = ModelHelper.faceFromIndex(cullFaceIdx);
            if (cullFace != null) {
                cullFace = r.resultingRotate(cullFace); // This fixes the incorrect lightmap position
            }

            var quads = quadCollection.apply(cullFace);
            for (var quad : quads) {
                // TODO 1.21.11: QuadTransforms at this point does not rotate direction
                var rotatedQuad = QuadTransforms.applyTransformation(quad, r.getTransformation());
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
