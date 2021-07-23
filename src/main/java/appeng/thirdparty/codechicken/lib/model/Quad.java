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

package appeng.thirdparty.codechicken.lib.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import com.mojang.math.Vector3f;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.IVertexProducer;
import net.minecraftforge.client.model.pipeline.LightUtil;

import appeng.thirdparty.codechicken.lib.math.InterpHelper;

/**
 * A simple easy to manipulate quad format. Can be reset and then used on a different format.
 *
 * @author covers1624
 */
public class Quad implements IVertexProducer, ISmartVertexConsumer {

    public CachedFormat format;

    public int tintIndex = -1;
    public Direction orientation;
    public boolean diffuseLighting = true;
    public TextureAtlasSprite sprite;

    public Vertex[] vertices = new Vertex[4];
    public boolean full;

    // Not copied.
    private int vertexIndex = 0;
    // Cache for normal computation.
    private Vector3f v1 = new Vector3f();
    private Vector3f v2 = new Vector3f();
    private Vector3f t = new Vector3f();
    private Vector3f normal = new Vector3f();

    /**
     * Use this if you reset the quad each time you use it.
     */
    public Quad() {
    }

    /**
     * use this if you want to initialize the quad with a format.
     *
     * @param format The format.
     */
    public Quad(CachedFormat format) {
        this.format = format;
    }

    @Override
    public VertexFormat getVertexFormat() {
        return this.format.format;
    }

    @Override
    public void setQuadTint(int tint) {
        this.tintIndex = tint;
    }

    @Override
    public void setQuadOrientation(Direction orientation) {
        this.orientation = orientation;
    }

    @Override
    public void setApplyDiffuseLighting(boolean diffuse) {
        this.diffuseLighting = diffuse;
    }

    @Override
    public void setTexture(TextureAtlasSprite texture) {
        this.sprite = texture;
    }

    @Override
    public void put(int element, float... data) {
        if (this.full) {
            throw new RuntimeException("Unable to add data when full.");
        }
        Vertex v = this.vertices[this.vertexIndex];
        if (v == null) {
            v = new Vertex(this.format);
            this.vertices[this.vertexIndex] = v;
        }
        System.arraycopy(data, 0, v.raw[element], 0, data.length);
        if (element == this.format.elementCount - 1) {
            this.vertexIndex++;
            if (this.vertexIndex == 4) {
                this.vertexIndex = 0;
                this.full = true;
                if (this.orientation == null) {
                    this.calculateOrientation(false);
                }
            }
        }
    }

    @Override
    public void put(Quad quad) {
        this.copyFrom(quad);
    }

    @Override
    public void pipe(IVertexConsumer consumer) {
        if (consumer instanceof ISmartVertexConsumer) {
            ((ISmartVertexConsumer) consumer).put(this);
        } else {
            consumer.setQuadTint(this.tintIndex);
            consumer.setQuadOrientation(this.orientation);
            consumer.setApplyDiffuseLighting(this.diffuseLighting);
            consumer.setTexture(this.sprite);
            for (Vertex v : this.vertices) {
                for (int e = 0; e < this.format.elementCount; e++) {
                    consumer.put(e, v.raw[e]);
                }
            }
        }
    }

    /**
     * Used to reset the interpolation values inside the provided helper.
     *
     * @param helper The helper.
     * @param s      The axis. side >> 1;
     * @return The same helper.
     */
    public InterpHelper resetInterp(InterpHelper helper, int s) {
        helper.reset( //
                this.vertices[0].dx(s), this.vertices[0].dy(s), //
                this.vertices[1].dx(s), this.vertices[1].dy(s), //
                this.vertices[2].dx(s), this.vertices[2].dy(s), //
                this.vertices[3].dx(s), this.vertices[3].dy(s));
        return helper;
    }

    /**
     * Clamps the Quad inside the box.
     *
     * @param bb The box.
     */
    public void clamp(AABB bb) {
        for (Vertex vertex : this.vertices) {
            float[] vec = vertex.vec;
            vec[0] = (float) Mth.clamp(vec[0], bb.minX, bb.maxX);
            vec[1] = (float) Mth.clamp(vec[1], bb.minY, bb.maxY);
            vec[2] = (float) Mth.clamp(vec[2], bb.minZ, bb.maxZ);
        }
        this.calculateOrientation(true);
    }

    /**
     * Re-calculates the Orientation of this quad, optionally the normal vector.
     *
     * @param setNormal If the normal vector should be updated.
     */
    public void calculateOrientation(boolean setNormal) {
        this.v1.set(this.vertices[3].vec);
        this.t.set(this.vertices[1].vec);
        this.v1.sub(this.t);

        this.v2.set(this.vertices[2].vec);
        this.t.set(this.vertices[0].vec);
        this.v2.sub(this.t);

        this.normal.set(this.v2.x(), this.v2.y(), this.v2.z());
        this.normal.cross(this.v1);
        this.normal.normalize();

        if (this.format.hasNormal && setNormal) {
            for (Vertex vertex : this.vertices) {
                vertex.normal[0] = this.normal.x();
                vertex.normal[1] = this.normal.y();
                vertex.normal[2] = this.normal.z();
                vertex.normal[3] = 0;
            }
        }
        this.orientation = Direction.getNearest(this.normal.x(), this.normal.y(), this.normal.z());
    }

    /**
     * Used to create a new quad complete copy of this one.
     *
     * @return The new quad.
     */
    public Quad copy() {
        if (!this.full) {
            throw new RuntimeException("Only copying full quads is supported.");
        }
        Quad quad = new Quad(this.format);
        quad.tintIndex = this.tintIndex;
        quad.orientation = this.orientation;
        quad.diffuseLighting = this.diffuseLighting;
        quad.sprite = this.sprite;
        quad.full = true;
        for (int i = 0; i < 4; i++) {
            quad.vertices[i] = this.vertices[i].copy();
        }
        return quad;
    }

    /**
     * Copies the data inside the given quad to this one. This ignores VertexFormat, please make sure your quads are in
     * the same format.
     *
     * @param quad The Quad to copy from.
     * @return This quad.
     */
    public Quad copyFrom(Quad quad) {
        this.tintIndex = quad.tintIndex;
        this.orientation = quad.orientation;
        this.diffuseLighting = quad.diffuseLighting;
        this.sprite = quad.sprite;
        this.full = quad.full;
        for (int v = 0; v < 4; v++) {
            for (int e = 0; e < this.format.elementCount; e++) {
                System.arraycopy(quad.vertices[v].raw[e], 0, this.vertices[v].raw[e], 0, 4);
            }
        }
        return this;
    }

    /**
     * Reset the quad to the new format.
     *
     * @param format The new format.
     */
    public void reset(CachedFormat format) {
        this.format = format;
        this.tintIndex = -1;
        this.orientation = null;
        this.diffuseLighting = true;
        this.sprite = null;
        for (int i = 0; i < this.vertices.length; i++) {
            Vertex v = this.vertices[i];
            if (v == null) {
                this.vertices[i] = v = new Vertex(format);
            }
            v.reset(format);
        }
        this.vertexIndex = 0;
        this.full = false;
    }

    /**
     * Bakes this Quad to a BakedQuad.
     *
     * @return The BakedQuad.
     */
    public BakedQuad bake() {
        if (format.format != DefaultVertexFormat.BLOCK) {
            throw new IllegalStateException("Unable to bake this quad to the specified format. " + format.format);
        }
        int[] packedData = new int[this.format.format.getVertexSize()];
        for (int v = 0; v < 4; v++) {
            for (int e = 0; e < this.format.elementCount; e++) {
                LightUtil.pack(this.vertices[v].raw[e], packedData, this.format.format, v, e);
            }
        }
        return new BakedQuad(packedData, this.tintIndex, this.orientation, this.sprite, this.diffuseLighting);
    }

    /**
     * A simple vertex format.
     */
    public static class Vertex {

        public CachedFormat format;

        /**
         * The raw data.
         */
        public float[][] raw;

        // References to the arrays inside raw.
        public float[] vec;
        public float[] normal;
        public float[] color;
        public float[] uv;
        public float[] overlay;
        public float[] lightmap;

        /**
         * Create a new Vertex.
         *
         * @param format The format for the vertex.
         */
        public Vertex(CachedFormat format) {
            this.format = format;
            this.raw = new float[format.elementCount][4];
            this.preProcess();
        }

        /**
         * Creates a new Vertex using the data inside the other. A copy!
         *
         * @param other The other.
         */
        public Vertex(Vertex other) {
            this.format = other.format;
            this.raw = other.raw.clone();
            for (int v = 0; v < this.format.elementCount; v++) {
                this.raw[v] = other.raw[v].clone();
            }
            this.preProcess();
        }

        /**
         * Pulls references to the individual element's arrays inside raw. Modifying the individual element arrays will
         * update raw.
         */
        public void preProcess() {
            if (this.format.hasPosition) {
                this.vec = this.raw[this.format.positionIndex];
            }
            if (this.format.hasNormal) {
                this.normal = this.raw[this.format.normalIndex];
            }
            if (this.format.hasColor) {
                this.color = this.raw[this.format.colorIndex];
            }
            if (this.format.hasUV) {
                this.uv = this.raw[this.format.uvIndex];
            }
            if (format.hasOverlay) {
                overlay = raw[format.overlayIndex];
            }
            if (this.format.hasLightMap) {
                this.lightmap = this.raw[this.format.lightMapIndex];
            }
        }

        /**
         * Gets the 2d X coord for the given axis.
         *
         * @param s The axis. side >> 1
         * @return The x coord.
         */
        public float dx(int s) {
            if (s <= 1) {
                return this.vec[0];
            } else {
                return this.vec[2];
            }
        }

        /**
         * Gets the 2d Y coord for the given axis.
         *
         * @param s The axis. side >> 1
         * @return The y coord.
         */
        public float dy(int s) {
            if (s > 0) {
                return this.vec[1];
            } else {
                return this.vec[2];
            }
        }

        /**
         * Interpolates the new color values for this Vertex using the others as a reference.
         *
         * @param interpHelper The InterpHelper to use.
         * @param others       The other Vertices to use as a template.
         * @return The same Vertex.
         */
        public Vertex interpColorFrom(InterpHelper interpHelper, Vertex[] others) {
            for (int e = 0; e < 4; e++) {
                float p1 = others[0].color[e];
                float p2 = others[1].color[e];
                float p3 = others[2].color[e];
                float p4 = others[3].color[e];
                // Only interpolate if colors are different.
                if (p1 != p2 || p2 != p3 || p3 != p4) {
                    this.color[e] = interpHelper.interpolate(p1, p2, p3, p4);
                }
            }
            return this;
        }

        /**
         * Interpolates the new UV values for this Vertex using the others as a reference.
         *
         * @param interpHelper The InterpHelper to use.
         * @param others       The other Vertices to use as a template.
         * @return The same Vertex.
         */
        public Vertex interpUVFrom(InterpHelper interpHelper, Vertex[] others) {
            for (int e = 0; e < 2; e++) {
                float p1 = others[0].uv[e];
                float p2 = others[1].uv[e];
                float p3 = others[2].uv[e];
                float p4 = others[3].uv[e];
                if (p1 != p2 || p2 != p3 || p3 != p4) {
                    this.uv[e] = interpHelper.interpolate(p1, p2, p3, p4);
                }
            }
            return this;
        }

        /**
         * Interpolates the new LightMap values for this Vertex using the others as a reference.
         *
         * @param interpHelper The InterpHelper to use.
         * @param others       The other Vertices to use as a template.
         * @return The same Vertex.
         */
        public Vertex interpLightMapFrom(InterpHelper interpHelper, Vertex[] others) {
            for (int e = 0; e < 2; e++) {
                float p1 = others[0].lightmap[e];
                float p2 = others[1].lightmap[e];
                float p3 = others[2].lightmap[e];
                float p4 = others[3].lightmap[e];
                if (p1 != p2 || p2 != p3 || p3 != p4) {
                    this.lightmap[e] = interpHelper.interpolate(p1, p2, p3, p4);
                }
            }
            return this;
        }

        /**
         * Copies this Vertex to a new one.
         *
         * @return The new Vertex.
         */
        public Vertex copy() {
            return new Vertex(this);
        }

        /**
         * Resets the Vertex to a new format. Expands the raw array if needed.
         *
         * @param format The format to reset to.
         */
        public void reset(CachedFormat format) {
            // If the format is different and our raw array is smaller, then expand it.
            if (!this.format.equals(format) && format.elementCount > this.raw.length) {
                this.raw = new float[format.elementCount][4];
            }
            this.format = format;

            this.vec = null;
            this.normal = null;
            this.color = null;
            this.uv = null;
            this.lightmap = null;

            // for (float[] f : raw) {
            // Arrays.fill(f, 0F);
            // }

            this.preProcess();
        }
    }
}
