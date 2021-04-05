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

package appeng.client.render.cablebus;

import java.util.EnumMap;
import java.util.EnumSet;

import com.google.common.base.Preconditions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector4f;

/**
 * Builds the quads for a cube.
 */
@Environment(EnvType.CLIENT)
public class CubeBuilder {

    private final EnumMap<Direction, TextureAtlasSprite> textures = new EnumMap<>(Direction.class);

    private EnumSet<Direction> drawFaces = EnumSet.allOf(Direction.class);

    private final EnumMap<Direction, Vector4f> customUv = new EnumMap<>(Direction.class);

    private final byte[] uvRotations = new byte[Direction.values().length];

    private int color = 0xFFFFFFFF;

    private boolean useStandardUV = false;

    private boolean emissiveMaterial;

    private final QuadEmitter emitter;

    private int vertexIndex = 0;

    public CubeBuilder(QuadEmitter emitter) {
        this.emitter = emitter;
    }

    public void addCube(float x1, float y1, float z1, float x2, float y2, float z2) {
        x1 /= 16.0f;
        y1 /= 16.0f;
        z1 /= 16.0f;
        x2 /= 16.0f;
        y2 /= 16.0f;
        z2 /= 16.0f;

        for (Direction face : this.drawFaces) {
            this.putFace(face, x1, y1, z1, x2, y2, z2);
        }
    }

    public void addQuad(Direction face, float x1, float y1, float z1, float x2, float y2, float z2) {
        this.putFace(face, x1, y1, z1, x2, y2, z2);
    }

    private static final class UvVector {
        float u1;
        float u2;
        float v1;
        float v2;
    }

    private void putFace(Direction face, float x1, float y1, float z1, float x2, float y2, float z2) {

        TextureAtlasSprite texture = this.textures.get(face);

        QuadEmitter emitter = this.emitter;
        emitter.colorIndex(-1).nominalFace(face);

        UvVector uv = new UvVector();

        // The user might have set specific UV coordinates for this face
        Vector4f customUv = this.customUv.get(face);
        if (customUv != null) {
            uv.u1 = texture.getInterpolatedU(customUv.getX());
            uv.v1 = texture.getInterpolatedV(customUv.getY());
            uv.u2 = texture.getInterpolatedU(customUv.getZ());
            uv.v2 = texture.getInterpolatedV(customUv.getW());
        } else if (this.useStandardUV) {
            uv = this.getStandardUv(face, texture, x1, y1, z1, x2, y2, z2);
        } else {
            uv = this.getDefaultUv(face, texture, x1, y1, z1, x2, y2, z2);
        }

        switch (face) {
            case DOWN:
                this.putVertexTR(face, x2, y1, z1, uv);
                this.putVertexBR(face, x2, y1, z2, uv);
                this.putVertexBL(face, x1, y1, z2, uv);
                this.putVertexTL(face, x1, y1, z1, uv);
                break;
            case UP:
                this.putVertexTL(face, x1, y2, z1, uv);
                this.putVertexBL(face, x1, y2, z2, uv);
                this.putVertexBR(face, x2, y2, z2, uv);
                this.putVertexTR(face, x2, y2, z1, uv);
                break;
            case NORTH:
                this.putVertexBR(face, x2, y2, z1, uv);
                this.putVertexTR(face, x2, y1, z1, uv);
                this.putVertexTL(face, x1, y1, z1, uv);
                this.putVertexBL(face, x1, y2, z1, uv);
                break;
            case SOUTH:
                this.putVertexBL(face, x1, y2, z2, uv);
                this.putVertexTL(face, x1, y1, z2, uv);
                this.putVertexTR(face, x2, y1, z2, uv);
                this.putVertexBR(face, x2, y2, z2, uv);
                break;
            case WEST:
                this.putVertexTL(face, x1, y1, z1, uv);
                this.putVertexTR(face, x1, y1, z2, uv);
                this.putVertexBR(face, x1, y2, z2, uv);
                this.putVertexBL(face, x1, y2, z1, uv);
                break;
            case EAST:
                this.putVertexBR(face, x2, y2, z1, uv);
                this.putVertexBL(face, x2, y2, z2, uv);
                this.putVertexTL(face, x2, y1, z2, uv);
                this.putVertexTR(face, x2, y1, z1, uv);
                break;
        }

        if (emissiveMaterial) {
            // Force Brightness to 15, this is for full bright mode
            // this vertex element will only be present in that case
            int lightmap = LightTexture.packLight(15, 15);
            emitter.lightmap(lightmap, lightmap, lightmap, lightmap);
        }

        emitter.emit();
        this.vertexIndex = 0;
    }

    private UvVector getDefaultUv(Direction face, TextureAtlasSprite texture, float x1, float y1, float z1, float x2,
            float y2,
            float z2) {

        UvVector uv = new UvVector();

        switch (face) {
            case DOWN:
                uv.u1 = texture.getInterpolatedU(x1 * 16);
                uv.v1 = texture.getInterpolatedV(z1 * 16);
                uv.u2 = texture.getInterpolatedU(x2 * 16);
                uv.v2 = texture.getInterpolatedV(z2 * 16);
                break;
            case UP:
                uv.u1 = texture.getInterpolatedU(x1 * 16);
                uv.v1 = texture.getInterpolatedV(z1 * 16);
                uv.u2 = texture.getInterpolatedU(x2 * 16);
                uv.v2 = texture.getInterpolatedV(z2 * 16);
                break;
            case NORTH:
                uv.u1 = texture.getInterpolatedU(x1 * 16);
                uv.v1 = texture.getInterpolatedV(16 - y1 * 16);
                uv.u2 = texture.getInterpolatedU(x2 * 16);
                uv.v2 = texture.getInterpolatedV(16 - y2 * 16);
                break;
            case SOUTH:
                uv.u1 = texture.getInterpolatedU(x1 * 16);
                uv.v1 = texture.getInterpolatedV(16 - y1 * 16);
                uv.u2 = texture.getInterpolatedU(x2 * 16);
                uv.v2 = texture.getInterpolatedV(16 - y2 * 16);
                break;
            case WEST:
                uv.u1 = texture.getInterpolatedU(z1 * 16);
                uv.v1 = texture.getInterpolatedV(16 - y1 * 16);
                uv.u2 = texture.getInterpolatedU(z2 * 16);
                uv.v2 = texture.getInterpolatedV(16 - y2 * 16);
                break;
            case EAST:
                uv.u1 = texture.getInterpolatedU(z2 * 16);
                uv.v1 = texture.getInterpolatedV(16 - y1 * 16);
                uv.u2 = texture.getInterpolatedU(z1 * 16);
                uv.v2 = texture.getInterpolatedV(16 - y2 * 16);
                break;
        }

        return uv;
    }

    private UvVector getStandardUv(Direction face, TextureAtlasSprite texture, float x1, float y1, float z1, float x2,
            float y2,
            float z2) {
        UvVector uv = new UvVector();
        switch (face) {
            case DOWN:
                uv.u1 = texture.getInterpolatedU(x1 * 16);
                uv.v1 = texture.getInterpolatedV(16 - z1 * 16);
                uv.u2 = texture.getInterpolatedU(x2 * 16);
                uv.v2 = texture.getInterpolatedV(16 - z2 * 16);
                break;
            case UP:
                uv.u1 = texture.getInterpolatedU(x1 * 16);
                uv.v1 = texture.getInterpolatedV(z1 * 16);
                uv.u2 = texture.getInterpolatedU(x2 * 16);
                uv.v2 = texture.getInterpolatedV(z2 * 16);
                break;
            case NORTH:
                uv.u1 = texture.getInterpolatedU(16 - x1 * 16);
                uv.v1 = texture.getInterpolatedV(16 - y1 * 16);
                uv.u2 = texture.getInterpolatedU(16 - x2 * 16);
                uv.v2 = texture.getInterpolatedV(16 - y2 * 16);
                break;
            case SOUTH:
                uv.u1 = texture.getInterpolatedU(x1 * 16);
                uv.v1 = texture.getInterpolatedV(16 - y1 * 16);
                uv.u2 = texture.getInterpolatedU(x2 * 16);
                uv.v2 = texture.getInterpolatedV(16 - y2 * 16);
                break;
            case WEST:
                uv.u1 = texture.getInterpolatedU(z1 * 16);
                uv.v1 = texture.getInterpolatedV(16 - y1 * 16);
                uv.u2 = texture.getInterpolatedU(z2 * 16);
                uv.v2 = texture.getInterpolatedV(16 - y2 * 16);
                break;
            case EAST:
                uv.u1 = texture.getInterpolatedU(16 - z2 * 16);
                uv.v1 = texture.getInterpolatedV(16 - y1 * 16);
                uv.u2 = texture.getInterpolatedU(16 - z1 * 16);
                uv.v2 = texture.getInterpolatedV(16 - y2 * 16);
                break;
        }
        return uv;
    }

    // uv.u1, uv.v1
    private void putVertexTL(Direction face, float x, float y, float z, UvVector uv) {
        float u, v;

        switch (this.uvRotations[face.ordinal()]) {
            default:
            case 0:
                u = uv.u1;
                v = uv.v1;
                break;
            case 1: // 90° clockwise
                u = uv.u1;
                v = uv.v2;
                break;
            case 2: // 180° clockwise
                u = uv.u2;
                v = uv.v2;
                break;
            case 3: // 270° clockwise
                u = uv.u2;
                v = uv.v1;
                break;
        }

        this.putVertex(face, x, y, z, u, v);
    }

    // uv.u2, uv.v1
    private void putVertexTR(Direction face, float x, float y, float z, UvVector uv) {
        float u, v;

        switch (this.uvRotations[face.ordinal()]) {
            default:
            case 0:
                u = uv.u2;
                v = uv.v1;
                break;
            case 1: // 90° clockwise
                u = uv.u1;
                v = uv.v1;
                break;
            case 2: // 180° clockwise
                u = uv.u1;
                v = uv.v2;
                break;
            case 3: // 270° clockwise
                u = uv.u2;
                v = uv.v2;
                break;
        }
        this.putVertex(face, x, y, z, u, v);
    }

    // uv.u2, uv.v2
    private void putVertexBR(Direction face, float x, float y, float z, UvVector uv) {

        float u;
        float v;

        switch (this.uvRotations[face.ordinal()]) {
            default:
            case 0:
                u = uv.u2;
                v = uv.v2;
                break;
            case 1: // 90° clockwise
                u = uv.u2;
                v = uv.v1;
                break;
            case 2: // 180° clockwise
                u = uv.u1;
                v = uv.v1;
                break;
            case 3: // 270° clockwise
                u = uv.u1;
                v = uv.v2;
                break;
        }

        this.putVertex(face, x, y, z, u, v);
    }

    // uv.u1, uv.v2
    private void putVertexBL(Direction face, float x, float y, float z, UvVector uv) {

        float u;
        float v;

        switch (this.uvRotations[face.ordinal()]) {
            default:
            case 0:
                u = uv.u1;
                v = uv.v2;
                break;
            case 1: // 90° clockwise
                u = uv.u2;
                v = uv.v2;
                break;
            case 2: // 180° clockwise
                u = uv.u2;
                v = uv.v1;
                break;
            case 3: // 270° clockwise
                u = uv.u1;
                v = uv.v1;
                break;
        }

        this.putVertex(face, x, y, z, u, v);
    }

    private void putVertex(Direction face, float x, float y, float z, float u, float v) {

        emitter.pos(vertexIndex, x, y, z);
        emitter.normal(vertexIndex, face.getXOffset(), face.getYOffset(), face.getZOffset());

        // Color format is RGBA
        emitter.spriteColor(vertexIndex, 0, this.color);

        emitter.sprite(vertexIndex, 0, u, v);

        vertexIndex++;
    }

    public void setTexture(TextureAtlasSprite texture) {
        for (Direction face : Direction.values()) {
            this.textures.put(face, texture);
        }
    }

    public void setTextures(TextureAtlasSprite up, TextureAtlasSprite down, TextureAtlasSprite north,
            TextureAtlasSprite south, TextureAtlasSprite east, TextureAtlasSprite west) {
        this.textures.put(Direction.UP, up);
        this.textures.put(Direction.DOWN, down);
        this.textures.put(Direction.NORTH, north);
        this.textures.put(Direction.SOUTH, south);
        this.textures.put(Direction.EAST, east);
        this.textures.put(Direction.WEST, west);
    }

    public void setTexture(Direction facing, TextureAtlasSprite sprite) {
        this.textures.put(facing, sprite);
    }

    public void setDrawFaces(EnumSet<Direction> drawFaces) {
        this.drawFaces = drawFaces;
    }

    public void setColor(int color) {
        this.color = color;
    }

    /**
     * Sets the vertex color for future vertices to the given RGB value, and forces the alpha component to 255.
     */
    public void setColorRGB(int color) {
        this.setColor(color | 0xFF000000);
    }

    public void setColorRGB(float r, float g, float b) {
        this.setColorRGB((int) (r * 255) << 16 | (int) (g * 255) << 8 | (int) (b * 255));
    }

    public void setEmissiveMaterial(boolean renderFullBright) {
        this.emissiveMaterial = renderFullBright;
    }

    public void setCustomUv(Direction facing, float u1, float v1, float u2, float v2) {
        this.customUv.put(facing, new Vector4f(u1, v1, u2, v2));
    }

    public void setUvRotation(Direction facing, int rotation) {
        if (rotation == 2) {
            rotation = 3;
        } else if (rotation == 3) {
            rotation = 2;
        }
        Preconditions.checkArgument(rotation >= 0 && rotation <= 3, "rotation");
        this.uvRotations[facing.ordinal()] = (byte) rotation;
    }

    /**
     * CubeBuilder uses UV optimized for cables by default. This switches to standard UV coordinates.
     */
    public void useStandardUV() {
        this.useStandardUV = true;
    }

}
