/*
 * This file is part of CodeChickenLib.
 * Copyright (c) 2018, covers1624, All rights reserved.
 *
 * CodeChickenLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * CodeChickenLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with CodeChickenLib. If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.thirdparty.codechicken.lib.model.pipeline.transformers;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

import appeng.thirdparty.codechicken.lib.math.InterpHelper;

/**
 * This transformer Re-Interpolates the Color, UV's and LightMaps. Use this after all transformations that translate
 * vertices in the pipeline.
 * <p>
 * This Transformation can only be used in the BakedPipeline.
 *
 * @author covers1624
 */
public class QuadReInterpolator implements RenderContext.QuadTransform {

    private final InterpHelper interpHelper = new InterpHelper();

    private final int[] originalSpriteColor = new int[4];

    private final float[] originalSpriteU = new float[4];

    private final float[] originalSpriteV = new float[4];

    public QuadReInterpolator() {
        super();
    }

    public void setInputQuad(QuadView quad) {
        int s = quad.nominalFace().ordinal() >> 1;
        int xIdx = dx(s);
        int yIdx = dy(s);
        interpHelper.reset( //
                quad.posByIndex(0, xIdx), quad.posByIndex(0, yIdx), //
                quad.posByIndex(1, xIdx), quad.posByIndex(1, yIdx), //
                quad.posByIndex(2, xIdx), quad.posByIndex(2, yIdx), //
                quad.posByIndex(3, xIdx), quad.posByIndex(3, yIdx));

        // Save the original properties of the quad's vertices
        for (int i = 0; i < 4; i++) {
            originalSpriteColor[i] = quad.spriteColor(i, 0);
            originalSpriteU[i] = quad.spriteU(i, 0);
            originalSpriteV[i] = quad.spriteV(i, 0);
        }
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        int s = quad.nominalFace().ordinal() >> 1;
        int xIdx = dx(s);
        int yIdx = dy(s);

        this.interpHelper.setup();
        for (int i = 0; i < 4; i++) {
            float x = quad.posByIndex(i, xIdx);
            float y = quad.posByIndex(i, yIdx);
            this.interpHelper.locate(x, y);
            interpColorFrom(quad, i);
            interpUVFrom(quad, i);
            interpLightMapFrom(quad, i);
        }
        return true;
    }

    /**
     * Interpolates the new color values for this Vertex using the others as a reference.
     */
    public void interpColorFrom(MutableQuadView quad, int vertexIndex) {
        int p1 = this.originalSpriteColor[0];
        int p2 = this.originalSpriteColor[1];
        int p3 = this.originalSpriteColor[2];
        int p4 = this.originalSpriteColor[3];
        if (p1 == p2 && p2 == p3 && p3 == p4) {
            return; // Don't bother for uniformly colored quads
        }

        // Interpolate each color component separately
        int color = 0;
        int mask = 0xFF;
        for (int i = 0; i < 4; i++) {
            float p1c = (float) (p1 & mask);
            float p2c = (float) (p2 & mask);
            float p3c = (float) (p3 & mask);
            float p4c = (float) (p4 & mask);
            int interp = (int) interpHelper.interpolate(p1c, p2c, p3c, p4c);
            color |= interp & mask;
            mask <<= 8;
        }

        quad.spriteColor(vertexIndex, 0, color);
    }

    /**
     * Interpolates the new UV values for this Vertex using the others as a reference.
     */
    public void interpUVFrom(MutableQuadView quad, int vertexIndex) {
        float p1 = originalSpriteU[0];
        float p2 = originalSpriteU[1];
        float p3 = originalSpriteU[2];
        float p4 = originalSpriteU[3];
        float u = interpHelper.interpolate(p1, p2, p3, p4);

        p1 = originalSpriteV[0];
        p2 = originalSpriteV[1];
        p3 = originalSpriteV[2];
        p4 = originalSpriteV[3];
        float v = interpHelper.interpolate(p1, p2, p3, p4);
        quad.sprite(vertexIndex, 0, u, v);
    }

    /**
     * Interpolates the new LightMap values for this Vertex using the others as a reference.
     *
     * @return The same Vertex.
     */
    public void interpLightMapFrom(MutableQuadView quad, int vertexIndex) {
        for (int e = 0; e < 2; e++) {
// FIXME           float p1 = others[0].lightmap[e];
// FIXME           float p2 = others[1].lightmap[e];
// FIXME           float p3 = others[2].lightmap[e];
// FIXME           float p4 = others[3].lightmap[e];
// FIXME           if (p1 != p2 || p2 != p3 || p3 != p4) {
// FIXME               this.lightmap[e] = interpHelper.interpolate(p1, p2, p3, p4);
// FIXME           }
        }
    }

    /**
     * Gets the 2d X coord for the given axis.
     *
     * @param s The axis. side >> 1
     * @return The x coord.
     */
    private static int dx(int s) {
        if (s <= 1) {
            return 0;
        } else {
            return 2;
        }
    }

    /**
     * Gets the 2d Y coord for the given axis.
     *
     * @param s The axis. side >> 1
     * @return The y coord.
     */
    private static int dy(int s) {
        if (s > 0) {
            return 1;
        } else {
            return 2;
        }
    }

}
