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
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Vector3f;

/**
 * Utility class to render LEDs for storage cells from a Block entity Renderer.
 */
public class CellLedRenderer {

    // The positions are based on the upper left slot in a drive
    private static final float L = 5 / 16.f; // left (x-axis)
    private static final float R = 4 / 16.f; // right (x-axis)
    private static final float T = 1 / 16.f; // top (y-axis)
    private static final float B = -0.001f / 16.f; // bottom (y-axis)
    private static final float FR = -0.001f / 16.f; // front (z-axis)
    private static final float BA = 0.999f / 16.f; // back (z-axis)

    // Vertex data for the LED cuboid (has no back)
    // Directions are when looking from the front onto the LED
    private static final float[] LED_QUADS = {
            // Front Face
            R, T, FR, L, T, FR, L, B, FR, R, B, FR,
            // Left Face
            L, T, FR, L, T, BA, L, B, BA, L, B, FR,
            // Right Face
            R, T, BA, R, T, FR, R, B, FR, R, B, BA,
            // Top Face
            R, T, BA, L, T, BA, L, T, FR, R, T, FR,
            // Bottom Face
            R, B, FR, L, B, FR, L, B, BA, R, B, BA, };

    public static void renderLed(Vector3f color, VertexConsumer buffer, PoseStack ms) {
        if (color == null) {
            return;
        }

        for (int i = 0; i < LED_QUADS.length; i += 3) {
            float x = LED_QUADS[i];
            float y = LED_QUADS[i + 1];
            float z = LED_QUADS[i + 2];
            buffer.addVertex(ms.last().pose(), x, y, z).setColor(color.x(), color.y(), color.z(), 1.f);
        }
    }

    private CellLedRenderer() {
    }

}
