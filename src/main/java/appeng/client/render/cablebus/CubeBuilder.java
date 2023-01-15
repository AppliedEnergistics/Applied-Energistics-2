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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Preconditions;

import org.joml.Vector4f;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import appeng.thirdparty.fabric.EncodingFormat;
import appeng.thirdparty.fabric.MutableQuadViewImpl;
import appeng.thirdparty.fabric.QuadEmitter;

/**
 * Builds the quads for a cube.
 */
public class CubeBuilder {

    private final List<BakedQuad> output;

    private final EnumMap<Direction, TextureAtlasSprite> textures = new EnumMap<>(Direction.class);

    private EnumSet<Direction> drawFaces = EnumSet.allOf(Direction.class);

    private final EnumMap<Direction, Vector4f> customUv = new EnumMap<>(Direction.class);

    private byte[] uvRotations = new byte[Direction.values().length];

    private final boolean[] flipU = new boolean[Direction.values().length];

    private final boolean[] flipV = new boolean[Direction.values().length];

    private int color = 0xFFFFFFFF;

    private boolean emissiveMaterial;

    public CubeBuilder(List<BakedQuad> output) {
        this.output = output;
    }

    public CubeBuilder() {
        this(new ArrayList<>(6));
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

    public void setFlipU(Direction side, boolean enable) {
        flipU[side.ordinal()] = enable;
    }

    public void setFlipV(Direction side, boolean enable) {
        flipV[side.ordinal()] = enable;
    }

    private static final class UvVector {
        float u1;
        float u2;
        float v1;
        float v2;
    }

    private void putFace(Direction face, float x1, float y1, float z1, float x2, float y2, float z2) {

        var texture = this.textures.get(face);

        var emitter = new MutableQuadViewImpl() {
            {
                begin(new int[EncodingFormat.TOTAL_STRIDE], 0);
            }

            @Override
            public QuadEmitter emit() {
                output.add(toBakedQuad(texture));
                return this;
            }
        };
        emitter.colorIndex(-1);

        var uv = new UvVector();

        // The user might have set specific UV coordinates for this face
        var customUv = this.customUv.get(face);
        if (customUv != null) {
            uv.u1 = texture.getU(customUv.x());
            uv.v1 = texture.getV(customUv.y());
            uv.u2 = texture.getU(customUv.z());
            uv.v2 = texture.getV(customUv.w());
        } else {
            uv = this.getStandardUv(face, texture, x1, y1, z1, x2, y2, z2);
        }

        emitter.color(color, color, color, color);
        emitter.normal(0, face.getStepX(), face.getStepY(), face.getStepZ());
        emitter.normal(1, face.getStepX(), face.getStepY(), face.getStepZ());
        emitter.normal(2, face.getStepX(), face.getStepY(), face.getStepZ());
        emitter.normal(3, face.getStepX(), face.getStepY(), face.getStepZ());

        setFaceUV(face, emitter, uv);

        switch (face) {
            case DOWN -> emitter.square(face, x1, z1, x2, z2, y1);
            case UP -> emitter.square(face, x1, 1 - z2, x2, 1 - z1, 1 - y2);
            case NORTH -> emitter.square(face, 1 - x2, y1, 1 - x1, y2, z1);
            case SOUTH -> emitter.square(face, x1, y1, x2, y2, 1 - z2);
            case WEST -> emitter.square(face, z1, y1, z2, y2, x1);
            case EAST -> emitter.square(face, 1 - z2, y1, 1 - z1, y2, 1 - x2);
        }

        if (emissiveMaterial) {
            // Force Brightness to 15, this is for full bright mode
            // this vertex element will only be present in that case
            int lightmap = LightTexture.pack(15, 15);
            emitter.lightmap(lightmap, lightmap, lightmap, lightmap);
        }

        emitter.emit();
    }

    private void setFaceUV(Direction face, QuadEmitter emitter, UvVector uv) {
        var rotation = uvRotations[face.ordinal()];

        if (flipU[face.ordinal()]) {
            var tmp = uv.u1;
            uv.u1 = uv.u2;
            uv.u2 = tmp;
        }
        if (flipV[face.ordinal()]) {
            var tmp = uv.v1;
            uv.v1 = uv.v2;
            uv.v2 = tmp;
        }

        switch (face) {
            case DOWN, UP -> {
                emitter.uv((0 + 4 - rotation) % 4, uv.u1, uv.v1);
                emitter.uv((1 + 4 - rotation) % 4, uv.u1, uv.v2);
                emitter.uv((2 + 4 - rotation) % 4, uv.u2, uv.v2);
                emitter.uv((3 + 4 - rotation) % 4, uv.u2, uv.v1);
            }
            case NORTH, SOUTH, WEST, EAST -> {
                emitter.uv((0 + 4 - rotation) % 4, uv.u1, uv.v2);
                emitter.uv((1 + 4 - rotation) % 4, uv.u1, uv.v1);
                emitter.uv((2 + 4 - rotation) % 4, uv.u2, uv.v1);
                emitter.uv((3 + 4 - rotation) % 4, uv.u2, uv.v2);
            }
        }
    }

    private UvVector getStandardUv(Direction face, TextureAtlasSprite texture, float x1, float y1, float z1, float x2,
            float y2, float z2) {
        UvVector uv = new UvVector();

        if (face.getAxis() != Direction.Axis.Y) {
            uv.v1 = texture.getV(16 - y1 * 16);
            uv.v2 = texture.getV(16 - y2 * 16);
        } else {
            uv.v1 = texture.getV(z1 * 16);
            uv.v2 = texture.getV(z2 * 16);
        }

        switch (face) {
            case DOWN, UP, SOUTH -> {
                uv.u1 = texture.getU(x1 * 16);
                uv.u2 = texture.getU(x2 * 16);
            }
            case NORTH -> {
                uv.u1 = texture.getU(16 - x2 * 16);
                uv.u2 = texture.getU(16 - x1 * 16);
            }
            case WEST -> {
                uv.u1 = texture.getU(z1 * 16);
                uv.u2 = texture.getU(z2 * 16);
            }
            case EAST -> {
                uv.u1 = texture.getU(16 - z2 * 16);
                uv.u2 = texture.getU(16 - z1 * 16);
            }
        }

        return uv;
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
        Preconditions.checkArgument(rotation >= 0 && rotation <= 3, "rotation");
        this.uvRotations[facing.ordinal()] = (byte) rotation;
    }

    public List<BakedQuad> getOutput() {
        return this.output;
    }
}
