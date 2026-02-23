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

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import org.joml.Vector3f;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.storage.cells.CellState;

/**
 * Utility class to render LEDs for storage cells from a Block entity Renderer.
 */
public class CellLedRenderer {

    private static final EnumMap<CellState, Vector3f> STATE_COLORS;

    // Color to use if the cell is present but unpowered
    private static final Vector3f UNPOWERED_COLOR = new Vector3f(0, 0, 0);

    // Idle brightness multiplier for non-full cells. Blinking pulses from this up to full brightness
    private static final float DIM_FACTOR = 0.55f;

    // Duration of one full cosine pulse in milliseconds
    private static final long PULSE_MS = 200;

    static {
        STATE_COLORS = new EnumMap<>(CellState.class);
        for (var cellState : CellState.values()) {
            var color = cellState.getStateColor();
            var colorVector = new Vector3f(
                    ((color >> 16) & 0xFF) / 255.0f,
                    ((color >> 8) & 0xFF) / 255.0f,
                    (color & 0xFF) / 255.0f);
            STATE_COLORS.put(cellState, colorVector);
        }
    }

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

    public static final RenderType RENDER_LAYER = RenderType.create("ae_drive_leds",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 32565, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                    .createCompositeState(false));

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
            buffer.addVertex(ms.last().pose(), x, y, z).setColor(color.x(), color.y(), color.z(), 1.f);
        }
    }

    private static Vector3f getColorForSlot(IChestOrDrive drive, int slot, float partialTicks) {
        var state = drive.getCellStatus(slot);
        if (state == CellState.ABSENT) {
            return null;
        }

        if (!drive.isPowered()) {
            return UNPOWERED_COLOR;
        }

        Vector3f bright = STATE_COLORS.get(state);

        // Full cells stay at full brightness permanently — they are a constant warning
        if (state == CellState.FULL) {
            return bright;
        }

        // All other states idle at a dimmer shade and pulse to full brightness on activity
        Vector3f dim = new Vector3f(bright).mul(DIM_FACTOR);
        if (drive.isCellBlinking(slot)) {
            float phase = (System.currentTimeMillis() % PULSE_MS) / (float) PULSE_MS;
            float f = 0.5f - 0.5f * (float) Math.cos(phase * 2 * Math.PI);
            dim.lerp(bright, f);
        }

        return dim;
    }

    private CellLedRenderer() {
    }

}
