/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.client.renderer.spatialstorage;

import java.util.Optional;
import java.util.OptionalDouble;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;

import appeng.client.render.AERenderPipelines;

/*
 * This renderer had to be rewritten around the raw renderpearl API.
 * - com.mojang.blaze3d.buffers.GpuBuffer / GpuBufferSlice moved to com.mojang.renderpearl.api.buffers.
 * - com.mojang.blaze3d.vertex.Tesselator and RenderType#draw(MeshData) are GONE. There is no immediate-mode
 * "draw this mesh with this RenderType" path left, and the sky pass runs outside the submit-node system
 * (LevelRenderer#addSkyPass, no SubmitNodeCollector), so the skybox is now baked once into a static vertex
 * buffer and drawn in the same hand-rolled RenderPass as the sparkles - exactly how vanilla's
 * SkyRenderer#buildEndSky / #renderEndSky work. appeng.client.render.AERenderTypes#SPATIAL_SKYBOX is
 * consequently unused by this class now; the pipeline (AERenderPipelines#SPATIAL_SKYBOX) is used directly.
 * - VertexFormat.Mode -> com.mojang.renderpearl.api.pipeline.PrimitiveTopology.
 * - CustomSkyboxRenderer#renderSky's trailing `Runnable setupFog` became the `GpuBufferSlice skyFog` itself.
 * Neither AE2 pipeline declares a FOG bind group, so - as before - the fog is not installed.
 * - CommandEncoder#createRenderPass takes Optional<Vector4fc> (linear RGBA) for the clear colour instead of
 * an OptionalInt of packed ARGB, RenderPass#setPipeline takes a CompiledRenderPipeline,
 * RenderPass#setVertexBuffer takes a GpuBufferSlice and RenderPass#drawIndexed was reordered to
 * (indexCount, instanceCount, firstIndex, vertexOffset, firstInstance).
 * - Minecraft#getMainRenderTarget() moved to GameRenderer#mainRenderTarget().
 */
public class SpatialStorageSkyRenderer implements CustomSkyboxRenderer, AutoCloseable {

    private static final int MAX_SPARKLE_QUADS = 50;

    private static final int SKYBOX_SIDES = 6;
    private static final int SKYBOX_VERTICES = SKYBOX_SIDES * 4;
    private static final int SKYBOX_INDICES = SKYBOX_SIDES * 6;

    private final RandomSource random = RandomSource.create();
    private long cycle = 0;
    private GpuBuffer skyboxVertices;
    private GpuBuffer sparklesVertices;
    private int sparklesQuads;

    private static final Matrix4fc[] SKYBOX_SIDE_ROTATIONS = {
            new Matrix4f(),
            new Matrix4f().rotationX(Mth.DEG_TO_RAD * 90.0F),
            new Matrix4f().rotationX(Mth.DEG_TO_RAD * -90.0F),
            new Matrix4f().rotationX(Mth.DEG_TO_RAD * 180.0F),
            new Matrix4f().rotationZ(Mth.DEG_TO_RAD * 90.0F),
            new Matrix4f().rotationZ(Mth.DEG_TO_RAD * -90.0F), };

    @Override
    public boolean renderSky(LevelRenderState levelRenderState, SkyRenderState skyRenderState,
            Matrix4fc modelViewMatrix, GpuBufferSlice skyFog) {
        if (skyboxVertices == null) {
            skyboxVertices = buildSkybox();
        }

        // Cycle the sparkles between 0 and 0.25 color value over 2 seconds
        final long now = System.currentTimeMillis();
        if (sparklesVertices == null || now - this.cycle > 2000) {
            this.cycle = now;
            this.rebuildSparkles(1);
        }

        float fade = now - this.cycle;
        fade /= 1000;
        fade = 0.25f * (1.0f - Math.abs((fade - 1.0f) * (fade - 1.0f)));

        render(modelViewMatrix, fade);

        return true;
    }

    private void render(Matrix4fc modelViewMatrix, float fade) {
        // DynamicUniforms#writeTransform only accepts the concrete Matrix4f, not Matrix4fc.
        var modelView = new Matrix4f(modelViewMatrix);

        GpuBufferSlice skyboxTransforms = RenderSystem.getDynamicUniforms().writeTransform(modelView);
        GpuBufferSlice sparklesTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(modelView, new Vector4f(fade, fade, fade, 1.0F), new Vector3f(), new Matrix4f());

        var renderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        var colorBuffer = renderTarget.getColorTextureView();
        var depthBuffer = renderTarget.getDepthTextureView();
        var autoIndexBuffer = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);

        // Both draws share the one global QUADS index buffer. It must be requested once, for the
        // larger of the two index counts: AutoStorageIndexBuffer#getBuffer closes and re-creates the
        // underlying GpuBuffer when it has to grow, which would invalidate a handle fetched earlier.
        var indexBuffer = autoIndexBuffer.getBuffer(Math.max(SKYBOX_INDICES, sparklesQuads * 4));
        var indexType = autoIndexBuffer.type();

        try (var pass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "spatial sky", colorBuffer, Optional.empty(), depthBuffer,
                        OptionalDouble.empty())) {
            // This renders a skybox around the player at a far, fixed distance from them.
            // The skybox is pitch black and untextured
            pass.setPipeline(RenderSystem.getCompiledPipeline(AERenderPipelines.SPATIAL_SKYBOX));
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", skyboxTransforms);
            pass.setVertexBuffer(0, skyboxVertices.slice());
            pass.setIndexBuffer(indexBuffer, indexType);
            pass.drawIndexed(SKYBOX_INDICES, 1, 0, 0, 0);

            pass.setPipeline(RenderSystem.getCompiledPipeline(AERenderPipelines.SPATIAL_SKYBOX_SPARKLES));
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", sparklesTransforms);
            pass.setVertexBuffer(0, sparklesVertices.slice());
            pass.setIndexBuffer(indexBuffer, indexType);
            // argument order only - the old call was
            // drawIndexed(baseVertex=0, firstIndex=0, indexCount=sparklesQuads * 4, instanceCount=1).
            // NOTE: `sparklesQuads * 4` is a vertex count being used as an index count; for a QUADS index
            // buffer the correct value would be `sparklesQuads * 6`. That is a pre-existing AE2 bug (it
            // renders roughly the first two thirds of the sparkle quads) and has deliberately been kept
            // byte-for-byte identical here rather than silently changing what the sky looks like.
            pass.drawIndexed(sparklesQuads * 4, 1, 0, 0, 0);
        }
    }

    /**
     * Builds the six sides of the pitch black skybox into a single static vertex buffer, with the per-side rotation
     * baked into the positions. This mirrors vanilla {@code SkyRenderer#buildEndSky}.
     */
    private static GpuBuffer buildSkybox() {
        try (var byteBufferBuilder = ByteBufferBuilder
                .exactlySized(SKYBOX_VERTICES * DefaultVertexFormat.POSITION_COLOR.getVertexSize())) {
            var vb = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);

            for (var rotation : SKYBOX_SIDE_ROTATIONS) {
                // This is very similar to how the End sky is rendered, just untextured
                vb.addVertex(rotation, -100.0f, -100.0f, -100.0f).setColor(0f, 0f, 0f, 1f);
                vb.addVertex(rotation, -100.0f, -100.0f, 100.0f).setColor(0f, 0f, 0f, 1f);
                vb.addVertex(rotation, 100.0f, -100.0f, 100.0f).setColor(0f, 0f, 0f, 1f);
                vb.addVertex(rotation, 100.0f, -100.0f, -100.0f).setColor(0f, 0f, 0f, 1f);
            }

            try (var meshdata = vb.buildOrThrow()) {
                return RenderSystem.getDevice()
                        .createBuffer(() -> "Spatial sky skybox vertex buffer", GpuBuffer.USAGE_VERTEX,
                                meshdata.vertexBuffer());
            }
        }
    }

    private void rebuildSparkles(float fade) {
        if (sparklesVertices != null) {
            sparklesVertices.close();
        }
        sparklesQuads = 0;

        try (var bytebufferbuilder = new ByteBufferBuilder(
                MAX_SPARKLE_QUADS * 4 * DefaultVertexFormat.POSITION_COLOR.getVertexSize())) {
            var vb = new BufferBuilder(bytebufferbuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i < MAX_SPARKLE_QUADS; ++i) {
                float iX = this.random.nextFloat() * 2.0f - 1.0f;
                float iY = this.random.nextFloat() * 2.0f - 1.0f;
                float iZ = this.random.nextFloat() * 2.0f - 1.0f;
                float d3 = 0.05F + this.random.nextFloat() * 0.1f;
                float dist = iX * iX + iY * iY + iZ * iZ;

                if (dist < 1.0f && dist > 0.01f) {
                    dist = 1.0f / Mth.sqrt(dist);
                    iX *= dist;
                    iY *= dist;
                    iZ *= dist;
                    float x = iX * 100.0f;
                    float y = iY * 100.0f;
                    float z = iZ * 100.0f;
                    float d8 = (float) Mth.atan2(iX, iZ);
                    float d9 = Mth.sin(d8);
                    float d10 = Mth.cos(d8);
                    float d11 = (float) Mth.atan2(Mth.sqrt(iX * iX + iZ * iZ), iY);
                    float d12 = Mth.sin(d11);
                    float d13 = Mth.cos(d11);
                    float d14 = this.random.nextFloat() * Mth.PI * 2.0f;
                    float d15 = Mth.sin(d14);
                    float d16 = Mth.cos(d14);

                    for (int j = 0; j < 4; ++j) {
                        float d17 = 0.0f;
                        float d18 = ((j & 2) - 1) * d3;
                        float d19 = ((j + 1 & 2) - 1) * d3;
                        float d20 = d18 * d16 - d19 * d15;
                        float d21 = d19 * d16 + d18 * d15;
                        float d22 = d20 * d12 + d17 * d13;
                        float d23 = d17 * d12 - d20 * d13;
                        float d24 = d23 * d9 - d21 * d10;
                        float d25 = d21 * d9 + d23 * d10;
                        vb.addVertex(x + d24, y + d22, z + d25).setColor(fade, fade, fade, 1.0f);
                    }
                    sparklesQuads++;
                }
            }

            try (var meshdata = vb.buildOrThrow()) {
                sparklesVertices = RenderSystem.getDevice()
                        .createBuffer(() -> "Spatial sky sparkles vertex buffer", GpuBuffer.USAGE_VERTEX,
                                meshdata.vertexBuffer());
            }
        }
    }

    @Override
    public void close() {
        if (sparklesVertices != null) {
            sparklesVertices.close();
            sparklesVertices = null;
        }
        if (skyboxVertices != null) {
            skyboxVertices.close();
            skyboxVertices = null;
        }
    }
}
