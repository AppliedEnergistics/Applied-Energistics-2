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

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.core.Direction;

/**
 * Created by covers1624 on 2/6/20.
 */
public class QuadMatrixTransformer implements RenderContext.QuadTransform {
    private static final Matrix4f identity;

    static {
        identity = new Matrix4f();
        identity.setIdentity();
    }

    private final Vector3f storage3 = new Vector3f();
    private final Vector4f storage = new Vector4f();
    private Matrix4f matrix;
    private boolean identityMatrix;

    QuadMatrixTransformer() {
        super();
    }

    public QuadMatrixTransformer(Matrix4f matrix) {
        this.matrix = matrix;
        this.identityMatrix = matrix.equals(identity);
    }

    public void setMatrix(Matrix4f matrix) {
        this.matrix = matrix;
        this.identityMatrix = matrix.equals(identity);
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        if (identityMatrix) {
            return true;
        }
        for (int i = 0; i < 4; i++) {
            quad.copyPos(i, storage3);
            storage.set(storage3.x(), storage3.y(), storage3.z(), 1);
            storage.transform(matrix);
            quad.pos(i, storage.x(), storage.y(), storage.z());

            if (quad.hasNormal(i)) {
                quad.copyNormal(i, storage3);
                storage.set(storage3.x(), storage3.y(), storage3.z(), 0);
                storage.transform(matrix);
                storage.normalize();
                quad.normal(i, storage.x(), storage.y(), storage.z());

                if (i == 0) {
                    Direction face = Direction.getNearest(storage.x(), storage.y(), storage.z());
                    quad.nominalFace(face);
                    quad.cullFace(face);
                }
            }
        }
        return true;
    }
}
