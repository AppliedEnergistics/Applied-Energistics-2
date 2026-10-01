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

package appeng.client.areaoverlay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

import appeng.client.render.AERenderTypes;
import appeng.core.AppEng;
import appeng.core.areaoverlay.AreaOverlayManager;
import appeng.core.areaoverlay.IAreaOverlayDataSource;

/**
 * This is based on the area render of https://github.com/TeamPneumatic/pnc-repressurized/
 */
public class AreaOverlayRenderer {

    private static final ContextKey<List<IAreaOverlayDataSource>> OVERLAY_AREAS = new ContextKey<>(
            AppEng.makeId("overlay_areas"));

    @SubscribeEvent
    public void extractRenderState(ExtractLevelRenderStateEvent event) {
        var visibleAreas = AreaOverlayManager.getInstance().getVisible();

        var areasInThisLevel = new ArrayList<IAreaOverlayDataSource>();
        for (var visibleArea : visibleAreas) {
            if (visibleArea.getOverlaySourceLocation().getLevel() == event.getLevel()) {
                areasInThisLevel.add(visibleArea);
            }
        }
        event.getRenderState().setRenderData(OVERLAY_AREAS, areasInThisLevel);
    }

    @SubscribeEvent
    public void submitCustomGeometry(SubmitCustomGeometryEvent event) {
        var levelRenderState = event.getLevelRenderState();
        var visibleAreas = levelRenderState.getRenderDataOrDefault(OVERLAY_AREAS, List.of());

        if (visibleAreas.isEmpty()) {
            return;
        }

        var collector = event.getSubmitNodeCollector();
        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();

        Vec3 projectedView = levelRenderState.cameraRenderState.pos;
        poseStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        var viewMatrix = new Matrix4f(levelRenderState.cameraRenderState.viewRotationMatrix);
        for (var visibleArea : visibleAreas) {
            submit(visibleArea, poseStack, viewMatrix, collector);
        }

        poseStack.popPose();
    }

    public void submit(IAreaOverlayDataSource area, PoseStack poseStack, Matrix4fc viewMatrix,
            SubmitNodeCollector collector) {
        Level level = area.getOverlaySourceLocation().getLevel();
        // The geometry is built later in the frame, so take a snapshot of the chunks now
        Set<ChunkPos> allChunks = Set.copyOf(area.getOverlayChunks());
        int minY = level.getMinY();
        int maxY = level.getMaxY();
        int areaColor = area.getOverlayColor();

        collector.submitCustomGeometry(poseStack, AERenderTypes.AREA_OVERLAY_LINE_OCCLUDED,
                (pose, builder) -> render(minY, maxY, allChunks, pose, viewMatrix, builder, true, 0x30ffffff));
        collector.submitCustomGeometry(poseStack, AERenderTypes.AREA_OVERLAY_FACE,
                (pose, builder) -> render(minY, maxY, allChunks, pose, viewMatrix, builder, false, areaColor));
        collector.submitCustomGeometry(poseStack, AERenderTypes.AREA_OVERLAY_LINE,
                (pose, builder) -> render(minY, maxY, allChunks, pose, viewMatrix, builder, true, areaColor));
    }

    private void render(int minY, int maxY, Collection<ChunkPos> allChunks, PoseStack.Pose pose,
            Matrix4fc viewMatrix, VertexConsumer builder, boolean renderLines, int color) {
        for (ChunkPos pos : allChunks) {
            Matrix4f posMat = new Matrix4f(pose.pose()).translate(pos.getMinBlockX(), 0, pos.getMinBlockZ());
            // Clipping lines against the camera plane requires knowing their position in view-space
            Matrix4f viewSpaceMat = renderLines ? new Matrix4f(viewMatrix).mul(posMat) : null;
            var edges = new EdgeWriter(builder, posMat, viewSpaceMat, color);
            addVertices(minY, maxY, allChunks, edges, pos, renderLines);
        }
    }

    private void addVertices(int minY, int maxY, Collection<ChunkPos> allChunks, EdgeWriter edges, ChunkPos pos,
            boolean renderLines) {
        // Render around a whole chunk
        float x1 = 0f;
        float x2 = 16f;
        float y1 = minY;
        float y2 = maxY;
        float z1 = 0f;
        float z2 = 16f;

        boolean noNorth = !allChunks.contains(new ChunkPos(pos.x(), pos.z() - 1));
        boolean noSouth = !allChunks.contains(new ChunkPos(pos.x(), pos.z() + 1));
        boolean noWest = !allChunks.contains(new ChunkPos(pos.x() - 1, pos.z()));
        boolean noEast = !allChunks.contains(new ChunkPos(pos.x() + 1, pos.z()));

        if (noNorth) {
            // Face North, Edge Bottom
            edges.add(x1, y1, z1, x2, y1, z1);
            // Face North, Edge Top
            edges.add(x2, y2, z1, x1, y2, z1);
        }

        if (noSouth) {
            // Face South, Edge Bottom
            edges.add(x2, y1, z2, x1, y1, z2);
            // Face South, Edge Top
            edges.add(x1, y2, z2, x2, y2, z2);
        }

        if (noWest) {
            // Face West, Edge Bottom
            edges.add(x1, y1, z1, x1, y1, z2);
            // Face West, Edge Top
            edges.add(x1, y2, z2, x1, y2, z1);
        }

        if (noEast) {
            // Face East, Edge Bottom
            edges.add(x2, y1, z2, x2, y1, z1);
            // Face East, Edge Top
            edges.add(x2, y2, z1, x2, y2, z2);
        }

        if (renderLines) {
            if (noNorth || noWest) {
                // Face North, Edge West
                edges.add(x1, y1, z1, x1, y2, z1);
            }

            if (noNorth || noEast) {
                // Face North, Edge East
                edges.add(x2, y2, z1, x2, y1, z1);
            }

            if (noSouth || noEast) {
                // Face South, Edge East
                edges.add(x2, y1, z2, x2, y2, z2);
            }
            if (noSouth || noWest) {
                // Face South, Edge West
                edges.add(x1, y2, z2, x1, y1, z2);
            }
        } else {
            // Bottom Face
            edges.add(x1, y1, z1, x2, y1, z1);
            edges.add(x2, y1, z2, x1, y1, z2);
        }
    }

    /**
     * Writes pairs of vertices, which are either a line segment or one half of a quad. Both vertices use the direction
     * from the first to the second vertex as their normal, which the line shader uses as the line direction.
     *
     * @param viewSpaceMat If not null, line segments are clipped against the camera plane, like vanilla does for gizmo
     *                     lines. The line shader cannot handle segments that end behind the camera and would render
     *                     them with a view-dependent width and jitter (i.e. the long vertical edges when looking up or
     *                     down).
     */
    private record EdgeWriter(VertexConsumer wr, Matrix4f posMat, @Nullable Matrix4f viewSpaceMat, int color) {

        private static final float LINE_WIDTH = 3f;
        private static final float NEAR_PLANE_Z = -0.05f;

        void add(float x1, float y1, float z1, float x2, float y2, float z2) {
            if (viewSpaceMat != null) {
                var tmp = new Vector3f();
                float startZ = viewSpaceMat.transformPosition(x1, y1, z1, tmp).z;
                float endZ = viewSpaceMat.transformPosition(x2, y2, z2, tmp).z;
                boolean startIsBehindCamera = startZ > NEAR_PLANE_Z;
                boolean endIsBehindCamera = endZ > NEAR_PLANE_Z;
                if (startIsBehindCamera && endIsBehindCamera) {
                    return;
                }
                if (startIsBehindCamera || endIsBehindCamera) {
                    float t = Mth.clamp((NEAR_PLANE_Z - startZ) / (endZ - startZ), 0f, 1f);
                    float ix = Mth.lerp(t, x1, x2);
                    float iy = Mth.lerp(t, y1, y2);
                    float iz = Mth.lerp(t, z1, z2);
                    if (startIsBehindCamera) {
                        x1 = ix;
                        y1 = iy;
                        z1 = iz;
                    } else {
                        x2 = ix;
                        y2 = iy;
                        z2 = iz;
                    }
                }
            }

            var normal = new Vector3f(x2 - x1, y2 - y1, z2 - z1);
            if (normal.lengthSquared() < 1e-12f) {
                return;
            }
            normal.normalize();

            wr.addVertex(posMat, x1, y1, z1).setColor(color).setNormal(normal.x, normal.y, normal.z)
                    .setLineWidth(LINE_WIDTH);
            wr.addVertex(posMat, x2, y2, z2).setColor(color).setNormal(normal.x, normal.y, normal.z)
                    .setLineWidth(LINE_WIDTH);
        }
    }
}
