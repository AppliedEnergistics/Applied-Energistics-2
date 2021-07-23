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

import com.mojang.math.Vector4f;
import net.minecraft.core.Direction;
import com.mojang.math.Matrix4f;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

import appeng.thirdparty.codechicken.lib.model.Quad;
import appeng.thirdparty.codechicken.lib.model.pipeline.IPipelineElementFactory;
import appeng.thirdparty.codechicken.lib.model.pipeline.QuadTransformer;

/**
 * Created by covers1624 on 2/6/20.
 */
public class QuadMatrixTransformer extends QuadTransformer {
    public static IPipelineElementFactory<QuadMatrixTransformer> FACTORY = QuadMatrixTransformer::new;
    private static final Matrix4f identity;

    static {
        identity = new Matrix4f();
        identity.setIdentity();
    }

    private final Vector4f storage = new Vector4f();
    private Matrix4f matrix;
    private boolean identityMatrix;

    QuadMatrixTransformer() {
        super();
    }

    public QuadMatrixTransformer(IVertexConsumer parent, Matrix4f matrix) {
        super(parent);
        this.matrix = matrix;
        this.identityMatrix = matrix.equals(identity);
    }

    public void setMatrix(Matrix4f matrix) {
        this.matrix = matrix;
        this.identityMatrix = matrix.equals(identity);
    }

    @Override
    public boolean transform() {
        if (identityMatrix) {
            return true;
        }
        for (Quad.Vertex vertex : this.quad.vertices) {
            storage.set(vertex.vec[0], vertex.vec[1], vertex.vec[2], 1);
            storage.transform(matrix);
            vertex.vec[0] = storage.x();
            vertex.vec[1] = storage.y();
            vertex.vec[2] = storage.z();

            storage.set(vertex.normal[0], vertex.normal[1], vertex.normal[2], 0);
            storage.transform(matrix);
            storage.normalize();
            vertex.normal[0] = storage.x();
            vertex.normal[1] = storage.y();
            vertex.normal[2] = storage.z();
        }
        Quad.Vertex v0 = quad.vertices[0];
        quad.orientation = Direction.getNearest(v0.normal[0], v0.normal[1], v0.normal[2]);
        return true;
    }
}
