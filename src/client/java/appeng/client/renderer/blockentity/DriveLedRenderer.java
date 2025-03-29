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

import appeng.client.render.AERenderTypes;
import appeng.client.render.model.DriveModel;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.storage.DriveBlockEntity;

/**
 * Renders the drive cell status indicators.
 */
@OnlyIn(Dist.CLIENT)
public class DriveLedRenderer implements BlockEntityRenderer<DriveBlockEntity> {

    public DriveLedRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DriveBlockEntity drive, float partialTicks, PoseStack ms, MultiBufferSource buffers,
            int combinedLightIn, int combinedOverlayIn, Vec3 cameraPosition) {

        if (drive.getCellCount() != 10) {
            throw new IllegalStateException("Expected drive to have 10 slots");
        }

        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);
        var blockOrientation = BlockOrientation.get(drive);
        ms.mulPose(blockOrientation.getQuaternion());
        ms.translate(-0.5, -0.5, -0.5);

        var buffer = buffers.getBuffer(AERenderTypes.STORAGE_CELL_LEDS);

        Vector3f slotTranslation = new Vector3f();
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 2; col++) {
                ms.pushPose();

                DriveModel.getSlotOrigin(row, col, slotTranslation);
                ms.translate(slotTranslation.x(), slotTranslation.y(), slotTranslation.z());

                int slot = row * 2 + col;
                CellLedRenderer.renderLed(drive, slot, buffer, ms, partialTicks);

                ms.popPose();
            }
        }

        ms.popPose();
    }

}
