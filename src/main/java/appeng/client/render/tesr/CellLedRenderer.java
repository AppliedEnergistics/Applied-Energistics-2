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

package appeng.client.render.tesr;

import java.util.EnumMap;

import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.math.Vector3f;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.block.storage.DriveSlotState;

/**
 * Utility class to render LEDs for storage cells from a Tile Entity Renderer.
 */
class CellLedRenderer {

    private static final EnumMap<DriveSlotState, Vector3f> STATE_COLORS;

    // Color used for the cell indicator for blinking during recent activity
    private static final Vector3f BLINK_COLOR = new Vector3f(1, 0.5f, 0.5f);

    static {
        STATE_COLORS = new EnumMap<>(DriveSlotState.class);
        STATE_COLORS.put(DriveSlotState.OFFLINE, new Vector3f(0, 0, 0));
        STATE_COLORS.put(DriveSlotState.ONLINE, new Vector3f(0, 1, 0));
        STATE_COLORS.put(DriveSlotState.NOT_EMPTY, new Vector3f(0f, 0.667f, 1));
        STATE_COLORS.put(DriveSlotState.TYPES_FULL, new Vector3f(1, 0.667f, 0));
        STATE_COLORS.put(DriveSlotState.FULL, new Vector3f(1, 0, 0));
    }

    // The positions are based on the upper left slot in a drive
    private static final float L = 5 / 16.f; // left (x-axis)
    private static final float R = 4 / 16.f; // right (x-axis)
    private static final float T = 1 / 16.f; // top (y-axis)
    private static final float B = -0.001f / 16.f; // bottom (y-axis)
    private static final float FR = -0.001f / 16.f; // front (z-axis)
    private static final float BA = 0.499f / 16.f; // back (z-axis)

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

    public static final RenderType RENDER_LAYER = RenderType.create("ae_drive_leds",
            DefaultVertexFormat.POSITION_COLOR, 7, 32565, false, true, CompositeState.builder().createCompositeState(false));

    public static void renderLed(IChestOrDrive drive, int slot, VertexConsumer buffer, PoseStack ms,
                                 float partialTicks) {

        Vector3f color = getColorForSlot(drive, slot, partialTicks);
        if (color == null) {
            return;
        }

        for (int i = 0; i < LED_QUADS.length; i += 3) {
            float x = LED_QUADS[i];
            float y = LED_QUADS[i + 1];
            float z = LED_QUADS[i + 2];
            buffer.vertex(ms.last().pose(), x, y, z).color(color.x(), color.y(), color.z(), 1.f)
                    .endVertex();
        }
    }

    private static Vector3f getColorForSlot(IChestOrDrive drive, int slot, float partialTicks) {
        DriveSlotState state = DriveSlotState.fromCellStatus(drive.getCellStatus(slot));
        if (state == DriveSlotState.EMPTY) {
            return null;
        }

        if (!drive.isPowered()) {
            return STATE_COLORS.get(DriveSlotState.OFFLINE);
        }

        Vector3f col = STATE_COLORS.get(state);
        if (drive.isCellBlinking(slot)) {
            // 200 ms interval (100ms to get to red, then 100ms back)
            long t = System.currentTimeMillis() % 200;
            float f = (t - 100) / 200.0f + 0.5f;
            f = easeInOutCubic(f);
            col = col.copy();
            col.lerp(BLINK_COLOR, f);
        }

        return col;
    }

    private static float easeInOutCubic(float x) {
        return x < 0.5f ? 4 * x * x * x : 1 - (float) Math.pow(-2 * x + 2, 3) / 2;
    }

    private CellLedRenderer() {
    }

}
