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

import org.joml.Vector3f;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/**
 * This transformer simply clamps the vertices inside the provided box. You probably want to Re-Interpolate the UV's,
 * Color, and Lmap, see {@link QuadReInterpolator}
 *
 * @author covers1624
 */
public class QuadClamper implements RenderContext.QuadTransform {

    private final AABB clampBounds;

    private final Vector3f pos = new Vector3f();

    public QuadClamper(AABB clampBounds) {
        this.clampBounds = clampBounds;
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        int s = quad.nominalFace().ordinal() >> 1;

        clamp(quad, this.clampBounds);

        // Check if the quad would be invisible and cull it.
        float x1 = quad.posByIndex(0, dx(s));
        float x2 = quad.posByIndex(1, dx(s));
        float x3 = quad.posByIndex(2, dx(s));
        float x4 = quad.posByIndex(3, dx(s));

        float y1 = quad.posByIndex(0, dy(s));
        float y2 = quad.posByIndex(1, dy(s));
        float y3 = quad.posByIndex(2, dy(s));
        float y4 = quad.posByIndex(3, dy(s));

        // These comparisons are safe as we are comparing clamped values.
        boolean flag1 = x1 == x2 && x2 == x3 && x3 == x4;
        boolean flag2 = y1 == y2 && y2 == y3 && y3 == y4;
        return !flag1 && !flag2;
    }

    private void clamp(MutableQuadView quad, AABB bb) {
        for (int i = 0; i < 4; i++) {
            quad.copyPos(i, pos);
            pos.set((float) Mth.clamp(pos.x(), bb.minX, bb.maxX),
                    (float) Mth.clamp(pos.y(), bb.minY, bb.maxY),
                    (float) Mth.clamp(pos.z(), bb.minZ, bb.maxZ));
            quad.pos(i, pos);
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
