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

package appeng.client.render.overlay;

import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.ChunkPos;

/**
 * This is based on the area render of https://github.com/TeamPneumatic/pnc-repressurized/
 */
public class OverlayRenderer {

    private IOverlayDataSource source;

    OverlayRenderer(IOverlayDataSource source) {
        this.source = source;
    }

    public void render(PoseStack matrixStack, MultiBufferSource buffer) {
        RenderType typeFaces = OverlayRenderType.getBlockHilightFace();
        render(matrixStack, buffer.getBuffer(typeFaces), false);
        OverlayRenderType.finishBuffer(buffer, typeFaces);

        RenderType typeLines = OverlayRenderType.getBlockHilightLine();
        render(matrixStack, buffer.getBuffer(typeLines), true);
        OverlayRenderType.finishBuffer(buffer, typeLines);
    }

    private void render(PoseStack matrixStack, VertexConsumer builder, boolean renderLines) {
        int[] cols = OverlayRenderType.decomposeColor(this.source.getOverlayColor());
        for (ChunkPos pos : this.source.getOverlayChunks()) {
            matrixStack.pushPose();
            matrixStack.translate(pos.getMinBlockX(), 0, pos.getMinBlockZ());
            Matrix4f posMat = matrixStack.last().pose();
            addVertices(builder, posMat, pos, cols, renderLines);
            matrixStack.popPose();
        }
    }

    private void addVertices(VertexConsumer wr, Matrix4f posMat, ChunkPos pos, int[] cols, boolean renderLines) {
        Set<ChunkPos> chunks = this.source.getOverlayChunks();

        // Render around a whole chunk
        float x1 = 0f;
        float x2 = 16f;
        float y1 = 0f;
        float y2 = 256f;
        float z1 = 0f;
        float z2 = 16f;

        boolean noNorth = !chunks.contains(new ChunkPos(pos.x, pos.z - 1));
        boolean noSouth = !chunks.contains(new ChunkPos(pos.x, pos.z + 1));
        boolean noWest = !chunks.contains(new ChunkPos(pos.x - 1, pos.z));
        boolean noEast = !chunks.contains(new ChunkPos(pos.x + 1, pos.z));

        if (noNorth) {
            // Face North, Edge Bottom
            wr.vertex(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.vertex(posMat, x2, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            // Face North, Edge Top
            wr.vertex(posMat, x2, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.vertex(posMat, x1, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        }

        if (noSouth) {
            // Face South, Edge Bottom
            wr.vertex(posMat, x2, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.vertex(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            // Face South, Edge Top
            wr.vertex(posMat, x1, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.vertex(posMat, x2, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        }

        if (noWest) {
            // Face West, Edge Bottom
            wr.vertex(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.vertex(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            // Face West, Edge Top
            wr.vertex(posMat, x1, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.vertex(posMat, x1, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        }

        if (noEast) {
            // Face East, Edge Bottom
            wr.vertex(posMat, x2, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.vertex(posMat, x2, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            // Face East, Edge Top
            wr.vertex(posMat, x2, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.vertex(posMat, x2, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        }

        if (renderLines) {
            if (noNorth || noWest) {
                // Face North, Edge West
                wr.vertex(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.vertex(posMat, x1, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            }

            if (noNorth || noEast) {
                // Face North, Edge East
                wr.vertex(posMat, x2, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.vertex(posMat, x2, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            }

            if (noSouth || noEast) {
                // Face South, Edge East
                wr.vertex(posMat, x2, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.vertex(posMat, x2, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            }
            if (noSouth || noWest) {
                // Face South, Edge West
                wr.vertex(posMat, x1, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.vertex(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            }
        } else {
            // Bottom Face
            wr.vertex(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.vertex(posMat, x2, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.vertex(posMat, x2, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.vertex(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        }

    }
}
