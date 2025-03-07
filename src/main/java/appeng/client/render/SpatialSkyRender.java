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

package appeng.client.render;

import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class SpatialSkyRender {

    private static final SpatialSkyRender INSTANCE = new SpatialSkyRender();

    private final RandomSource random = RandomSource.create();
    private final VertexBuffer sparkleBuffer;
    private long cycle = 0;

    private static final RenderType RENDER_TYPE_SKYBOX = RenderType.CompositeRenderType.create(
            "ae2_spatial_sky_skybox",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            8192,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TransparencyStateShard.NO_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.WriteMaskStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    private static final RenderType RENDER_TYPE_SPARKLES = RenderType.CompositeRenderType.create(
            "ae2_spatial_sky_sparkles",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            8192,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TransparencyStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.WriteMaskStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    public SpatialSkyRender() {
        sparkleBuffer = new VertexBuffer(BufferUsage.DYNAMIC_WRITE);
    }

    public static SpatialSkyRender getInstance() {
        return INSTANCE;
    }

    private static final Quaternionf[] SKYBOX_SIDE_ROTATIONS = {new Quaternionf(),
            new Quaternionf().rotationX(Mth.DEG_TO_RAD * 90.0F),
            new Quaternionf().rotationX(Mth.DEG_TO_RAD * -90.0F), new Quaternionf().rotationX(Mth.DEG_TO_RAD * 180.0F),
            new Quaternionf().rotationZ(Mth.DEG_TO_RAD * 90.0F),
            new Quaternionf().rotationZ(Mth.DEG_TO_RAD * -90.0F),};

    public void render(Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        final long now = System.currentTimeMillis();
        if (now - this.cycle > 2000) {
            this.cycle = now;
            this.rebuildSparkles();
        }

        var poseStack = new PoseStack();
        poseStack.mulPose(modelViewMatrix);

        RENDER_TYPE_SKYBOX.setupRenderState();

        // This renders a skybox around the player at a far, fixed distance from them.
        // The skybox is pitch black and untextured
        for (Quaternionf rotation : SKYBOX_SIDE_ROTATIONS) {
            poseStack.pushPose();
            poseStack.mulPose(rotation);

            // This is very similar to how the End sky is rendered, just untextured
            Matrix4f matrix4f = poseStack.last().pose();
            var builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            builder.addVertex(matrix4f, -100.0f, -100.0f, -100.0f).setColor(0f, 0f, 0f, 1f);
            builder.addVertex(matrix4f, -100.0f, -100.0f, 100.0f).setColor(0f, 0f, 0f, 1f);
            builder.addVertex(matrix4f, 100.0f, -100.0f, 100.0f).setColor(0f, 0f, 0f, 1f);
            builder.addVertex(matrix4f, 100.0f, -100.0f, -100.0f).setColor(0f, 0f, 0f, 1f);
            BufferUploader.drawWithShader(builder.buildOrThrow());
            poseStack.popPose();
        }

        RENDER_TYPE_SKYBOX.clearRenderState();

        // Cycle the sparkles between 0 and 0.25 color value over 2 seconds
        float fade = now - this.cycle;
        fade /= 1000;
        fade = 0.25f * (1.0f - Math.abs((fade - 1.0f) * (fade - 1.0f)));

        if (fade > 0.0f) {
            RenderSystem.setShaderColor(fade, fade, fade, 1.0f);

            RenderSystem.getModelViewStack().pushMatrix();
            RenderSystem.getModelViewStack().set(modelViewMatrix);

            sparkleBuffer.bind();
            sparkleBuffer.drawWithRenderType(RENDER_TYPE_SPARKLES);
            VertexBuffer.unbind();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            RenderSystem.getModelViewStack().popMatrix();
        }
    }

    private void rebuildSparkles() {
        var vb = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < 50; ++i) {
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
                    vb.addVertex(x + d24, y + d22, z + d25).setColor(255, 255, 255, 255);
                }
            }
        }

        sparkleBuffer.bind();
        sparkleBuffer.upload(vb.buildOrThrow());
        VertexBuffer.unbind();
    }
}
