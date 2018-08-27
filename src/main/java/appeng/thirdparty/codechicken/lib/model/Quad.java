package appeng.thirdparty.codechicken.lib.model;

import appeng.thirdparty.codechicken.lib.math.InterpHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.IVertexProducer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

import javax.vecmath.Vector3f;
import java.util.Arrays;

/**
 * A simple easy to manipulate quad format.
 * Can be reset and then used on a different format.
 */
public class Quad implements IVertexProducer, ISmartVertexConsumer {

    public CachedFormat format;

    public int tintIndex = -1;
    //TODO, sometimes this is null because people don't do models properly.
    public EnumFacing orientation;
    public boolean diffuseLighting = true;
    public TextureAtlasSprite sprite;

    public Vertex[] vertices = new Vertex[4];
    public boolean full;

    //Not copied.
    private int vertexIndex = 0;
    //Cache for normal computation.
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

    //@formatter:off
    //Simple overrides. Compact because reasons.
    @Override public VertexFormat getVertexFormat() { return format.format; }
    @Override public void setQuadTint(int tint) { this.tintIndex = tint; }
    @Override public void setQuadOrientation(EnumFacing orientation) { this.orientation = orientation; }
    @Override public void setApplyDiffuseLighting(boolean diffuse) { this.diffuseLighting = diffuse; }
    @Override public void setTexture(TextureAtlasSprite texture) { this.sprite = texture; }
    //@formatter:on

    @Override
    public void put(int element, float... data) {
        if (full) {
            throw new RuntimeException("Unable to add data when full.");
        }
        Vertex v = vertices[vertexIndex];
        if (v == null) {
            v = new Vertex(format);
            vertices[vertexIndex] = v;
        }
        System.arraycopy(data, 0, v.raw[element], 0, data.length);
        if (element == (format.elementCount - 1)) {
            vertexIndex++;
            if (vertexIndex == 4) {
                vertexIndex = 0;
                full = true;
            }
        }
    }

    @Override
    public void put(Quad quad) {
        copyFrom(quad);
    }

    @Override
    public void pipe(IVertexConsumer consumer) {
        if (consumer instanceof ISmartVertexConsumer) {
            ((ISmartVertexConsumer) consumer).put(this);
        } else {
            consumer.setQuadTint(tintIndex);
            consumer.setQuadOrientation(orientation);
            consumer.setApplyDiffuseLighting(diffuseLighting);
            consumer.setTexture(sprite);
            for (Vertex v : vertices) {
                for (int e = 0; e < format.elementCount; e++) {
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
        helper.reset(//
                vertices[0].dx(s), vertices[0].dy(s),//
                vertices[1].dx(s), vertices[1].dy(s),//
                vertices[2].dx(s), vertices[2].dy(s),//
                vertices[3].dx(s), vertices[3].dy(s));
        return helper;
    }

    /**
     * Clamps the Quad inside the box.
     *
     * @param bb The box.
     */
    public void clamp(AxisAlignedBB bb) {
        for (Vertex vertex : vertices) {
            float[] vec = vertex.vec;
            vec[0] = (float) MathHelper.clamp(vec[0], bb.minX, bb.maxX);
            vec[1] = (float) MathHelper.clamp(vec[1], bb.minY, bb.maxY);
            vec[2] = (float) MathHelper.clamp(vec[2], bb.minZ, bb.maxZ);
        }

        v1.set(vertices[3].vec);
        t.set(vertices[1].vec);
        v1.sub(t);

        v2.set(vertices[2].vec);
        t.set(vertices[0].vec);
        v2.sub(t);

        normal.cross(v2, v1);
        normal.normalize();

        if (format.hasNormal) {
            for (Vertex vertex : vertices) {
                vertex.normal[0] = normal.x;
                vertex.normal[1] = normal.y;
                vertex.normal[2] = normal.z;
                vertex.normal[3] = 0;

            }
        }
        orientation = EnumFacing.getFacingFromVector(normal.x, normal.y, normal.z);
    }

    /**
     * Used to create a new quad complete copy of this one.
     *
     * @return The new quad.
     */
    public Quad copy() {
        if (!full) {
            throw new RuntimeException("Only copying full quads is supported.");
        }
        Quad quad = new Quad(format);
        quad.tintIndex = tintIndex;
        quad.orientation = orientation;
        quad.diffuseLighting = diffuseLighting;
        quad.sprite = sprite;
        quad.full = true;
        for (int i = 0; i < 4; i++) {
            quad.vertices[i] = vertices[i].copy();
        }
        return quad;
    }

    /**
     * Copies the data inside the given quad to this one.
     * This ignores VertexFormat, please make sure your quads are in the same format.
     *
     * @param quad The Quad to copy from.
     * @return This quad.
     */
    public Quad copyFrom(Quad quad) {
        tintIndex = quad.tintIndex;
        orientation = quad.orientation;
        diffuseLighting = quad.diffuseLighting;
        sprite = quad.sprite;
        full = quad.full;
        for (int v = 0; v < 4; v++) {
            for (int e = 0; e < format.elementCount; e++) {
                System.arraycopy(quad.vertices[v].raw[e], 0, vertices[v].raw[e], 0, 4);
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
        tintIndex = -1;
        orientation = null;
        diffuseLighting = true;
        sprite = null;
        for (int i = 0; i < vertices.length; i++) {
            Vertex v = vertices[i];
            if(v == null) {
                vertices[i] = v = new Vertex(format);
            }
            v.reset(format);
        }
        vertexIndex = 0;
        full = false;
    }

    /**
     * Bakes this Quad to a BakedQuad.
     *
     * @return The BakedQuad.
     */
    public BakedQuad bake() {
        int[] packedData = new int[format.format.getNextOffset()];
        for (int v = 0; v < 4; v++) {
            for (int e = 0; e < format.elementCount; e++) {
                LightUtil.pack(vertices[v].raw[e], packedData, format.format, v, e);
            }
        }
        return new BakedQuad(packedData, tintIndex, orientation, sprite, diffuseLighting, format.format);
    }

    /**
     * Bakes this quad to an UnpackedBakedQuad.
     *
     * @return The UnpackedBakedQuad.
     */
    public UnpackedBakedQuad bakeUnpacked() {
        UnpackedBakedQuad.Builder quad = new UnpackedBakedQuad.Builder(format.format);
        pipe(quad);
        return quad.build();
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

        //References to the arrays inside raw.
        public float[] vec;
        public float[] normal;
        public float[] color;
        public float[] uv;
        public float[] lightmap;

        /**
         * Create a new Vertex.
         *
         * @param format The format for the vertex.
         */
        public Vertex(CachedFormat format) {
            this.format = format;
            raw = new float[format.elementCount][4];
            preProcess();
        }

        /**
         * Creates a new Vertex using the data inside the other.
         * A copy!
         *
         * @param other The other.
         */
        public Vertex(Vertex other) {
            this.format = other.format;
            raw = other.raw.clone();
            for (int v = 0; v < format.elementCount; v++) {
                raw[v] = other.raw[v].clone();
            }
            preProcess();
        }

        /**
         * Pulls references to the individual element's arrays inside raw.
         * Modifying the individual element arrays will update raw.
         */
        public void preProcess() {
            if (format.hasPosition) {
                vec = raw[format.positionIndex];
            }
            if (format.hasNormal) {
                normal = raw[format.normalIndex];
            }
            if (format.hasColor) {
                color = raw[format.colorIndex];
            }
            if (format.hasUV) {
                uv = raw[format.uvIndex];
            }
            if (format.hasLightMap) {
                lightmap = raw[format.lightMapIndex];
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
                return vec[0];
            } else {
                return vec[2];
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
                return vec[1];
            } else {
                return vec[2];
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
                //Only interpolate if colors are different.
                if (p1 != p2 || p2 != p3 || p3 != p4) {
                    color[e] = interpHelper.interpolate(p1, p2, p3, p4);
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
                    uv[e] = interpHelper.interpolate(p1, p2, p3, p4);
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
                    lightmap[e] = interpHelper.interpolate(p1, p2, p3, p4);
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
         * Resets the Vertex to a new format.
         * Expands the raw array if needed.
         *
         * @param format The format to reset to.
         */
        public void reset(CachedFormat format) {
            //If the format is different and our raw array is smaller, then expand it.
            if (!this.format.equals(format) && format.elementCount > raw.length) {
                raw = new float[format.elementCount][4];
            }
            this.format = format;

            vec = null;
            normal = null;
            color = null;
            uv = null;
            lightmap = null;

//            for (float[] f : raw) {
//                Arrays.fill(f, 0F);
//            }

            preProcess();
        }
    }
}
