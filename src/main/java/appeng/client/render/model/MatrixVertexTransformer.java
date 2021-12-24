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

package appeng.client.render.model;

import java.util.List;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexFormatElement.Usage;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.pipeline.QuadGatheringTransformer;

/**
 * Applies an arbitrary transformation matrix to the vertices of a quad.
 */
final class MatrixVertexTransformer extends QuadGatheringTransformer {

    private final Matrix4f transform;

    public MatrixVertexTransformer(Matrix4f transform) {
        this.transform = transform;
    }

    @Override
    protected void processQuad() {
        VertexFormat format = this.parent.getVertexFormat();
        List<VertexFormatElement> elements = format.getElements();
        int count = elements.size();

        for (int v = 0; v < 4; v++) {
            for (int e = 0; e < count; e++) {
                VertexFormatElement element = elements.get(e);
                if (element.getUsage() == Usage.POSITION) {
                    this.parent.put(e, this.transform(this.quadData[e][v], element.getElementCount()));
                } else if (element.getUsage() == Usage.NORMAL) {
                    this.parent.put(e, this.transformNormal(this.quadData[e][v]));
                } else {
                    this.parent.put(e, this.quadData[e][v]);
                }
            }
        }
    }

    @Override
    public void setQuadTint(int tint) {
        this.parent.setQuadTint(tint);
    }

    @Override
    public void setQuadOrientation(Direction orientation) {
        this.parent.setQuadOrientation(orientation);
    }

    @Override
    public void setApplyDiffuseLighting(boolean diffuse) {
        this.parent.setApplyDiffuseLighting(diffuse);
    }

    @Override
    public void setTexture(TextureAtlasSprite texture) {
        this.parent.setTexture(texture);
    }

    private float[] transform(float[] fs, int elemCount) {
        switch (fs.length) {
            case 3:
                Vector4f vec = new Vector4f(fs[0], fs[1], fs[2], 1);
                vec.setX(vec.x() - 0.5f);
                vec.setY(vec.y() - 0.5f);
                vec.setZ(vec.z() - 0.5f);
                vec.transform(this.transform); // FIXME: Check this, we're using a Vec4, input is Vec3
                vec.setX(vec.x() + 0.5f);
                vec.setY(vec.y() + 0.5f);
                vec.setZ(vec.z() + 0.5f);
                return new float[] { vec.x(), vec.y(), vec.z() };
            case 4:
                Vector4f vecc = new Vector4f(fs[0], fs[1], fs[2], fs[3]);
                // Otherwise all translation is lost
                if (elemCount == 3) {
                    vecc.setW(1);
                }
                vecc.setX(vecc.x() - 0.5f);
                vecc.setY(vecc.y() - 0.5f);
                vecc.setZ(vecc.z() - 0.5f);
                vecc.transform(this.transform);
                vecc.setX(vecc.x() + 0.5f);
                vecc.setY(vecc.y() + 0.5f);
                vecc.setZ(vecc.z() + 0.5f);
                return new float[] { vecc.x(), vecc.y(), vecc.z(), vecc.w() };

            default:
                return fs;
        }
    }

    private float[] transformNormal(float[] fs) {
        Vector4f normal;

        switch (fs.length) {
            case 3:
                normal = new Vector4f(fs[0], fs[1], fs[2], 0);
                normal.transform(this.transform);
                normal.normalize();
                return new float[] { normal.x(), normal.y(), normal.z() };

            case 4:
                normal = new Vector4f(fs[0], fs[1], fs[2], fs[3]);
                normal.transform(this.transform);
                normal.normalize();
                return new float[] { normal.x(), normal.y(), normal.z(), normal.w() };

            default:
                return fs;
        }
    }
}
