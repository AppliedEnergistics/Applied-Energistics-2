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


import appeng.client.render.FacingToRotation;
import appeng.core.AELog;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;


/**
 * Assuming a default-orientation of forward=NORTH and up=UP, this class rotates a given list of quads to the desired
 * facing
 */
public class QuadRotator {

    public List<BakedQuad> rotateQuads(List<BakedQuad> quads, EnumFacing newForward, EnumFacing newUp) {
        if (newForward == EnumFacing.NORTH && newUp == EnumFacing.UP) {
            return quads; // This is the default orientation
        }

        List<BakedQuad> result = new ArrayList<>(quads.size());

        for (BakedQuad quad : quads) {
            result.add(this.rotateQuad(quad, newForward, newUp));
        }

        return result;
    }

    private BakedQuad rotateQuad(BakedQuad quad, EnumFacing forward, EnumFacing up) {
        // Sanitize forward/up
        if (forward.getAxis() == up.getAxis()) {
            if (up.getAxis() == EnumFacing.Axis.Y) {
                up = EnumFacing.NORTH;
            } else {
                up = EnumFacing.UP;
            }
        }

        FacingToRotation rotation = FacingToRotation.get(forward, up);
        Matrix4f mat = rotation.getMat();

        // Clone the vertex data used by the quad
        int[] newData = quad.getVertexData().clone();

        // Figure out where the position is in the array
        VertexFormat format = quad.getFormat();
        int posIdx = this.findPositionOffset(format) / 4;
        int stride = format.getNextOffset() / 4;
        int normalIdx = format.getNormalOffset();
        VertexFormatElement.EnumType normalType = null;
        // Figure out the type of the normals
        if (normalIdx != -1) {
            for (int i = 0; i < format.getElements().size(); i++) {
                VertexFormatElement element = format.getElement(i);
                if (element.getUsage() == VertexFormatElement.EnumUsage.NORMAL) {
                    normalType = element.getType();
                }
            }
        }

        for (int i = 0; i < 4; i++) {
            Point3f pos = new Point3f(Float.intBitsToFloat(newData[i * stride + posIdx]) - 0.5f, Float
                    .intBitsToFloat(newData[i * stride + posIdx + 1]) - 0.5f, Float.intBitsToFloat(newData[i * stride + posIdx + 2]) - 0.5f);

            // Rotate stuff around
            mat.transform(pos);

            // Write back
            newData[i * stride + posIdx] = Float.floatToIntBits(pos.getX() + 0.5f);
            newData[i * stride + posIdx + 1] = Float.floatToIntBits(pos.getY() + 0.5f);
            newData[i * stride + posIdx + 2] = Float.floatToIntBits(pos.getZ() + 0.5f);

            // Transform the normal if one is present
            if (normalIdx != -1) {
                if (normalType == VertexFormatElement.EnumType.FLOAT) {
                    Vector3f normal = new Vector3f(Float.intBitsToFloat(newData[i * stride + normalIdx]), Float
                            .intBitsToFloat(newData[i * stride + normalIdx + 1]), Float.intBitsToFloat(newData[i * stride + normalIdx + 2]));

                    // Rotate stuff around
                    mat.transform(normal);

                    // Write back
                    newData[i * stride + normalIdx] = Float.floatToIntBits(normal.getX());
                    newData[i * stride + normalIdx + 1] = Float.floatToIntBits(normal.getY());
                    newData[i * stride + normalIdx + 2] = Float.floatToIntBits(normal.getZ());
                } else if (normalType == VertexFormatElement.EnumType.BYTE) {
                    int idx = i * stride * 4 + normalIdx;
                    Vector3f normal = new Vector3f(getByte(newData, idx) / 127.0f, getByte(newData, idx + 1) / 127.0f, getByte(newData,
                            idx + 2) / 127.0f);

                    // Rotate stuff around
                    mat.transform(normal);

                    // Write back
                    setByte(newData, idx, (int) (normal.getX() * 127));
                    setByte(newData, idx + 1, (int) (normal.getY() * 127));
                    setByte(newData, idx + 2, (int) (normal.getZ() * 127));
                } else {
                    AELog.warn("Unsupported normal format: {}", normalType);
                }
            }
        }

        EnumFacing newFace = rotation.rotate(quad.getFace());
        return new BakedQuad(newData, quad.getTintIndex(), newFace, quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat());
    }

    private static int getByte(int[] data, int offset) {
        int idx = offset / 4;
        int subOffset = offset % 4;
        return (byte) (data[idx] >> (subOffset * 8));
    }

    private static void setByte(int[] data, int offset, int value) {
        int idx = offset / 4;
        int subOffset = offset % 4;
        int mask = 0xFF << (subOffset * 8);
        data[idx] = data[idx] & (~mask) | ((value & 0xFF) << (subOffset * 8));
    }

    private int findPositionOffset(VertexFormat format) {
        List<VertexFormatElement> elements = format.getElements();
        for (int i = 0; i < elements.size(); i++) {
            VertexFormatElement e = elements.get(i);
            if (e.isPositionElement()) {
                if (e.getType() != VertexFormatElement.EnumType.FLOAT) {
                    throw new IllegalArgumentException("Only floating point positions are supported");
                }
                return i;
            }
        }

        throw new IllegalArgumentException("Vertex format " + format + " has no position attribute!");
    }
}
