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

import static net.minecraft.core.Direction.AxisDirection.POSITIVE;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/**
 * This transformer strips quads that are on faces. Simply set the bounds for the faces, and the strip mask.
 *
 * @author covers1624
 */
public class QuadFaceStripper implements RenderContext.QuadTransform {

    private AABB bounds;
    private int mask;

    QuadFaceStripper() {
        super();
    }

    public QuadFaceStripper(AABB bounds, int mask) {
        this.bounds = bounds;
        this.mask = mask;
    }

    /**
     * The bounds of the faces, used as the .. bounds, if all vertices of a quad lay on the bounds, it is up for
     * stripping.
     *
     * @param bounds The bounds.
     */
    public void setBounds(AABB bounds) {
        this.bounds = bounds;
    }

    /**
     * The mask to strip edges. This is an opt in system, the mask is simple 'mask = (1 << side)'.
     *
     * @param mask The mask.
     */
    public void setMask(int mask) {
        this.mask = mask;
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        if (this.mask == 0) {
            return true;// No mask, nothing changes.
        }
        // If the bit for this quad is set, then check if we should strip.
        Direction face = quad.nominalFace();
        if ((this.mask & 1 << face.ordinal()) != 0) {
            Direction.AxisDirection dir = face.getAxisDirection();
            switch (face.getAxis()) {
                case X: {
                    float bound = (float) (dir == POSITIVE ? this.bounds.maxX : this.bounds.minX);
                    float x1 = quad.posByIndex(0, 0);
                    float x2 = quad.posByIndex(1, 0);
                    float x3 = quad.posByIndex(2, 0);
                    float x4 = quad.posByIndex(3, 0);
                    return x1 != x2 || x2 != x3 || x3 != x4 || x4 != bound;
                }
                case Y: {
                    float bound = (float) (dir == POSITIVE ? this.bounds.maxY : this.bounds.minY);
                    float y1 = quad.posByIndex(0, 1);
                    float y2 = quad.posByIndex(1, 1);
                    float y3 = quad.posByIndex(2, 1);
                    float y4 = quad.posByIndex(3, 1);
                    return y1 != y2 || y2 != y3 || y3 != y4 || y4 != bound;
                }
                case Z: {
                    float bound = (float) (dir == POSITIVE ? this.bounds.maxZ : this.bounds.minZ);
                    float z1 = quad.posByIndex(0, 2);
                    float z2 = quad.posByIndex(1, 2);
                    float z3 = quad.posByIndex(2, 2);
                    float z4 = quad.posByIndex(3, 2);
                    return z1 != z2 || z2 != z3 || z3 != z4 || z4 != bound;
                }
            }
        }
        return true;
    }
}
