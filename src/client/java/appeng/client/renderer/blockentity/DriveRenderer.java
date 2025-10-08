/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;

import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.client.render.AERenderTypes;
import appeng.client.render.model.DriveModel;

/**
 * Renders the drive cell status indicators.
 */
public class DriveRenderer implements BlockEntityRenderer<DriveBlockEntity, ChestOrDriveRenderState> {

    public DriveRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public ChestOrDriveRenderState createRenderState() {
        return new ChestOrDriveRenderState();
    }

    @Override
    public void extractRenderState(DriveBlockEntity drive, ChestOrDriveRenderState state, float partialTicks,
            Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(drive, state, partialTicks, cameraPos, crumblingOverlay);
        if (drive.getCellCount() != 10) {
            throw new IllegalStateException("Expected drive to have 10 slots");
        }

        var blockOrientation = BlockOrientation.get(drive);
        state.extract(blockOrientation, drive, partialTicks);
    }

    @Override
    public void submit(ChestOrDriveRenderState state, PoseStack poseStack, SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(state.blockOrientation.getQuaternion());
        poseStack.translate(-0.5, -0.5, -0.5);

        Vector3f slotTranslation = new Vector3f();
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 2; col++) {
                poseStack.pushPose();

                DriveModel.getSlotOrigin(row, col, slotTranslation);
                poseStack.translate(slotTranslation.x(), slotTranslation.y(), slotTranslation.z());

                int slot = row * 2 + col;
                nodes.submitCustomGeometry(
                        poseStack,
                        AERenderTypes.STORAGE_CELL_LEDS,
                        (pose, consumer) -> CellLedRenderer.renderLed(state.cellColors[slot], consumer, pose));

                poseStack.popPose();
            }
        }

        poseStack.popPose();
    }

}
