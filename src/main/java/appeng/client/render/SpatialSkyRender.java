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

import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.math.Quaternion;

public class SpatialSkyRender {

    private static final SpatialSkyRender INSTANCE = new SpatialSkyRender();

    private final Random random = new Random();
    private final VertexBuffer sparkleBuffer;
    private long cycle = 0;

    public SpatialSkyRender() {
        sparkleBuffer = new VertexBuffer();
    }

    public static SpatialSkyRender getInstance() {
        return INSTANCE;
    }

    private static final Quaternion[] SKYBOX_SIDE_ROTATIONS = { Quaternion.ONE, new Quaternion(90.0F, 0.0F, 0.0F, true),
            new Quaternion(-90.0F, 0.0F, 0.0F, true), new Quaternion(180.0F, 0.0F, 0.0F, true),
            new Quaternion(0.0F, 0.0F, 90.0F, true), new Quaternion(0.0F, 0.0F, -90.0F, true), };

    public void render(PoseStack matrixStack, Matrix4f projectionMatrix) {
        final long now = System.currentTimeMillis();
        if (now - this.cycle > 2000) {
            this.cycle = now;
            this.rebuildSparkles();
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(false);
        final Tesselator tessellator = Tesselator.getInstance();
        var buffer = tessellator.getBuilder();

        // This renders a skybox around the player at a far, fixed distance from them.
        // The skybox is pitch black and untextured
        for (Quaternion rotation : SKYBOX_SIDE_ROTATIONS) {
            matrixStack.pushPose();
            matrixStack.mulPose(rotation);

            // This is very similar to how the End sky is rendered, just untextured
            Matrix4f matrix4f = matrixStack.last().pose();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buffer.vertex(matrix4f, -100.0f, -100.0f, -100.0f).color(0f, 0f, 0f, 1f).endVertex();
            buffer.vertex(matrix4f, -100.0f, -100.0f, 100.0f).color(0f, 0f, 0f, 1f).endVertex();
            buffer.vertex(matrix4f, 100.0f, -100.0f, 100.0f).color(0f, 0f, 0f, 1f).endVertex();
            buffer.vertex(matrix4f, 100.0f, -100.0f, -100.0f).color(0f, 0f, 0f, 1f).endVertex();
            tessellator.end();
            matrixStack.popPose();
        }

        // Cycle the sparkles between 0 and 0.25 color value over 2 seconds
        float fade = now - this.cycle;
        fade /= 1000;
        fade = 0.25f * (1.0f - Math.abs((fade - 1.0f) * (fade - 1.0f)));

        if (fade > 0.0f) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            RenderSystem.setShaderColor(fade, fade, fade, 1.0f);
            sparkleBuffer.drawWithShader(matrixStack.last().pose(), projectionMatrix, GameRenderer.getPositionColorShader());
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
    }

    private void rebuildSparkles() {
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vb = tessellator.getBuilder();
        vb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < 50; ++i) {
            double iX = this.random.nextFloat() * 2.0F - 1.0F;
            double iY = this.random.nextFloat() * 2.0F - 1.0F;
            double iZ = this.random.nextFloat() * 2.0F - 1.0F;
            final double d3 = 0.05F + this.random.nextFloat() * 0.1F;
            double dist = iX * iX + iY * iY + iZ * iZ;

            if (dist < 1.0D && dist > 0.01D) {
                dist = 1.0D / Math.sqrt(dist);
                iX *= dist;
                iY *= dist;
                iZ *= dist;
                final double x = iX * 100.0D;
                final double y = iY * 100.0D;
                final double z = iZ * 100.0D;
                final double d8 = Math.atan2(iX, iZ);
                final double d9 = Math.sin(d8);
                final double d10 = Math.cos(d8);
                final double d11 = Math.atan2(Math.sqrt(iX * iX + iZ * iZ), iY);
                final double d12 = Math.sin(d11);
                final double d13 = Math.cos(d11);
                final double d14 = this.random.nextDouble() * Math.PI * 2.0D;
                final double d15 = Math.sin(d14);
                final double d16 = Math.cos(d14);

                for (int j = 0; j < 4; ++j) {
                    final double d17 = 0.0D;
                    final double d18 = ((j & 2) - 1) * d3;
                    final double d19 = ((j + 1 & 2) - 1) * d3;
                    final double d20 = d18 * d16 - d19 * d15;
                    final double d21 = d19 * d16 + d18 * d15;
                    final double d22 = d20 * d12 + d17 * d13;
                    final double d23 = d17 * d12 - d20 * d13;
                    final double d24 = d23 * d9 - d21 * d10;
                    final double d25 = d21 * d9 + d23 * d10;
                    vb.vertex(x + d24, y + d22, z + d25).color(255, 255, 255, 255).endVertex();
                }
            }
        }
        vb.end();

        sparkleBuffer.upload(vb);
    }
}
