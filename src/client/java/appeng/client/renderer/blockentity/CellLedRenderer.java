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

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import org.joml.Vector3f;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import appeng.client.render.AERenderTypes;
import appeng.core.AppEng;

/**
 * Utility class to render LEDs for storage cells from a Block entity Renderer. The model is positioned for the upper
 * left slot in a drive, uses a white sprite tinted with the LED color and is declared emissive.
 */
public class CellLedRenderer {

    public static final Identifier LED_MODEL_ID = AppEng.makeId("block/drive_cell_led");
    public static final StandaloneModelKey<BlockStateModelPart> LED_MODEL = new StandaloneModelKey<>(
            LED_MODEL_ID::toString);

    public static void renderLed(Vector3f color, BlockStateModelPart ledModel, PoseStack poseStack,
            SubmitNodeCollector nodes, int lightCoords) {
        if (color == null) {
            return;
        }

        nodes.submitBlockModel(
                poseStack,
                AERenderTypes.STORAGE_CELL_LEDS,
                List.of(ledModel),
                new int[] { ARGB.colorFromFloat(1.f, color.x(), color.y(), color.z()) },
                lightCoords,
                OverlayTexture.NO_OVERLAY,
                0);
    }

    private CellLedRenderer() {
    }

}
