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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Matrix4f;

/**
 * This is based on the area render of https://github.com/TeamPneumatic/pnc-repressurized/
 */
public class OverlayRenderer {
    private final Set<ChunkPos> showingPositions;
    private final int color;
    private final boolean disableDepthTest;

    OverlayRenderer(Set<ChunkPos> area, int color, float size, boolean disableDepthTest, boolean drawShapes) {
        this.showingPositions = area;
        this.color = color;
        this.disableDepthTest = disableDepthTest;
    }

    OverlayRenderer(Set<ChunkPos> area, int color, boolean disableDepthTest) {
        this(area, color, 0.5f, disableDepthTest, true);
    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer) {
//        RenderType type = OverlayRenderType.getBlockHilightFace(disableDepthTest);
//        render(matrixStack, buffer.getBuffer(type));
//        OverlayRenderType.finishBuffer(buffer, type);

        RenderType type = OverlayRenderType.getBlockHilightLine(disableDepthTest);
        render(matrixStack, buffer.getBuffer(type));
        OverlayRenderType.finishBuffer(buffer, type);
    }

    private void render(MatrixStack matrixStack, IVertexBuilder builder) {
        int[] cols = OverlayRenderType.decomposeColor(color);
        for (ChunkPos pos : showingPositions) {
            matrixStack.push();
            matrixStack.translate(pos.getXStart(), 0, pos.getZStart());
            Matrix4f posMat = matrixStack.getLast().getMatrix();
            addVertices(builder, posMat, pos, cols);
            matrixStack.pop();
        }
    }

    private void addVertices(IVertexBuilder wr, Matrix4f posMat, ChunkPos pos, int[] cols) {
        // Render around a whole chunk
        float x1 = 0f;
        float x2 = 16f;
        float y1 = 0f;
        float y2 = 256f;
        float z1 = 0f;
        float z2 = 16f;

        boolean noNorth = !this.showingPositions.contains(new ChunkPos(pos.x, pos.z - 1));
        boolean noSouth = !this.showingPositions.contains(new ChunkPos(pos.x, pos.z + 1));
        boolean noWest = !this.showingPositions.contains(new ChunkPos(pos.x - 1, pos.z));
        boolean noEast = !this.showingPositions.contains(new ChunkPos(pos.x + 1, pos.z));

        if (noNorth || noWest) {
            // Face North, Edge West
            wr.pos(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, x1, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        }

        if (noNorth || noEast) {
            // Face North, Edge East
            wr.pos(posMat, x2, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, x2, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        }

        if (noNorth) {
            // Face North, Edge Bottom
            wr.pos(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, x2, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            // Face North, Edge Top
            wr.pos(posMat, x2, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, x1, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        }

        if (noSouth || noEast) {
            // Face South, Edge East
            wr.pos(posMat, x2, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, x2, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        }
        if (noSouth || noWest) {
            // Face South, Edge West
            wr.pos(posMat, x1, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        }

        if (noSouth) {
            // Face South, Edge Bottom
            wr.pos(posMat, x2, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            // Face South, Edge Top
            wr.pos(posMat, x1, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, x2, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        }

        if (noWest) {
            // Face West, Edge Bottom
            wr.pos(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            // Face West, Edge Top
            wr.pos(posMat, x1, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, x1, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        }

        if (noEast) {
            // Face East, Edge Top
            wr.pos(posMat, x2, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, x2, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            // Face East, Edge Bottom
            wr.pos(posMat, x2, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, x2, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        }

    }
}
