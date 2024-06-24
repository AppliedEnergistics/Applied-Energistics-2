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

import org.joml.Matrix4f;

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

    public void render(PoseStack poseStack, MultiBufferSource buffer) {
        RenderType typeLinesOccluded = OverlayRenderType.getBlockHilightLineOccluded();
        render(poseStack, buffer.getBuffer(typeLinesOccluded), true, 0x30ffffff);

        RenderType typeFaces = OverlayRenderType.getBlockHilightFace();
        render(poseStack, buffer.getBuffer(typeFaces), false, this.source.getOverlayColor());

        RenderType typeLines = OverlayRenderType.getBlockHilightLine();
        render(poseStack, buffer.getBuffer(typeLines), true, this.source.getOverlayColor());
    }

    private void render(PoseStack poseStack, VertexConsumer builder, boolean renderLines, int color) {
        int[] cols = OverlayRenderType.decomposeColor(color);
        for (ChunkPos pos : this.source.getOverlayChunks()) {
            poseStack.pushPose();
            poseStack.translate(pos.getMinBlockX(), 0, pos.getMinBlockZ());
            Matrix4f posMat = poseStack.last().pose();
            addVertices(builder, posMat, pos, cols, renderLines);
            poseStack.popPose();
        }
    }

    private void addVertices(VertexConsumer wr, Matrix4f posMat, ChunkPos pos, int[] cols, boolean renderLines) {
        Set<ChunkPos> chunks = this.source.getOverlayChunks();

        // Render around a whole chunk
        float x1 = 0f;
        float x2 = 16f;
        float y1 = source.getOverlaySourceLocation().getLevel().getMinBuildHeight();
        float y2 = source.getOverlaySourceLocation().getLevel().getMaxBuildHeight();
        float z1 = 0f;
        float z2 = 16f;

        boolean noNorth = !chunks.contains(new ChunkPos(pos.x, pos.z - 1));
        boolean noSouth = !chunks.contains(new ChunkPos(pos.x, pos.z + 1));
        boolean noWest = !chunks.contains(new ChunkPos(pos.x - 1, pos.z));
        boolean noEast = !chunks.contains(new ChunkPos(pos.x + 1, pos.z));

        if (noNorth) {
            // Face North, Edge Bottom
            wr.addVertex(posMat, x1, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(1, 0, 0);
            wr.addVertex(posMat, x2, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(1, 0, 0);
            // Face North, Edge Top
            wr.addVertex(posMat, x2, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(-1, 0, 0);
            wr.addVertex(posMat, x1, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(-1, 0, 0);
        }

        if (noSouth) {
            // Face South, Edge Bottom
            wr.addVertex(posMat, x2, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(-1, 0, 0);
            wr.addVertex(posMat, x1, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(-1, 0, 0);
            // Face South, Edge Top
            wr.addVertex(posMat, x1, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(1, 0, 0);
            wr.addVertex(posMat, x2, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(1, 0, 0);
        }

        if (noWest) {
            // Face West, Edge Bottom
            wr.addVertex(posMat, x1, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(0, 0, 1);
            wr.addVertex(posMat, x1, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(0, 0, 1);
            // Face West, Edge Top
            wr.addVertex(posMat, x1, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(0, 0, -1);
            wr.addVertex(posMat, x1, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(0, 0, -1);
        }

        if (noEast) {
            // Face East, Edge Bottom
            wr.addVertex(posMat, x2, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(0, 0, -1);
            wr.addVertex(posMat, x2, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(0, 0, -1);
            // Face East, Edge Top
            wr.addVertex(posMat, x2, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(0, 0, 1);
            wr.addVertex(posMat, x2, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(0, 0, 1);
        }

        if (renderLines) {
            if (noNorth || noWest) {
                // Face North, Edge West
                wr.addVertex(posMat, x1, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0])
                        .setNormal(0, 1, 0);
                wr.addVertex(posMat, x1, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0])
                        .setNormal(0, 1, 0);
            }

            if (noNorth || noEast) {
                // Face North, Edge East
                wr.addVertex(posMat, x2, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0])
                        .setNormal(0, -1, 0);
                wr.addVertex(posMat, x2, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0])
                        .setNormal(0, -1, 0);
            }

            if (noSouth || noEast) {
                // Face South, Edge East
                wr.addVertex(posMat, x2, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0])
                        .setNormal(0, 1, 0);
                wr.addVertex(posMat, x2, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0])
                        .setNormal(0, 1, 0);
            }
            if (noSouth || noWest) {
                // Face South, Edge West
                wr.addVertex(posMat, x1, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0])
                        .setNormal(0, -1, 0);
                wr.addVertex(posMat, x1, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0])
                        .setNormal(0, -1, 0);
            }
        } else {
            // Bottom Face
            wr.addVertex(posMat, x1, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(1, 0, 0);
            wr.addVertex(posMat, x2, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(1, 0, 0);
            wr.addVertex(posMat, x2, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(-1, 0, 0);
            wr.addVertex(posMat, x1, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0])
                    .setNormal(-1, 0, 0);
        }

    }
}
