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

import static net.minecraft.util.Direction.AxisDirection.field_11060;
import static net.minecraft.util.Direction.AxisDirection.field_11056;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3i;

/**
 * This transformer is a little complicated. Basically a Facade / Cover can use this to 'kick' the edges in of quads to
 * fix z-Fighting in the corners. Use it by specifying the side of the block you are on, the bitmask for where the other
 * Facades / Cover's are, the bounding box of the facade, NOT the hole piece, and the thickness of your Facade / Cover,
 * this is used as the kick amount.
 *
 * @author covers1624
 */
public class QuadCornerKicker implements RenderContext.QuadTransform {

    public static final QuadCornerKicker INSTANCE = new QuadCornerKicker();

    // Simple horizonal lookups.
    public static int[][] horizonals = new int[][] {
            // Around Y axis, NSWE.
            { 2, 3, 4, 5 }, //
            { 2, 3, 4, 5 }, //

            // Around Z axis, DUWE.
            { 0, 1, 4, 5 }, //
            { 0, 1, 4, 5 }, //

            // Around X axis, DUNS.
            { 0, 1, 2, 3 }, //
            { 0, 1, 2, 3 } };

    private int mySide;
    private int facadeMask;
    private AxisAlignedBB box;
    private double thickness;

    public QuadCornerKicker() {
        super();
    }

    /**
     * Set's the side this Facade / Cover is attached to.
     *
     * @param side The side.
     */
    public void setSide(int side) {
        this.mySide = side;
    }

    /**
     * Sets the bitmask of Facades / Covers in the blockspace. This is as simple as, mask = (1 << side)
     *
     * @param mask The mask.
     */
    public void setFacadeMask(int mask) {
        this.facadeMask = mask;
    }

    /**
     * Sets the bounding box of the Facade / Cover, this should be the full box, not just a piece of the hole's 'ring'.
     *
     * @param box The BoundingBox.
     */
    public void setBox(AxisAlignedBB box) {
        this.box = box;
    }

    /**
     * Sets the amount to kick the vertex in by, this is your facades thickness.
     *
     * @param thickness The thickness.
     */
    public void setThickness(double thickness) {
        this.thickness = thickness;
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        int side = quad.nominalFace().ordinal();
        if (side != this.mySide && side != (this.mySide ^ 1)) {
            for (int hoz : horizonals[this.mySide]) {
                if (side != hoz && side != (hoz ^ 1)) {
                    if ((this.facadeMask & (1 << hoz)) != 0) {
                        Corner corner = Corner.fromSides(this.mySide ^ 1, side, hoz);
                        for (int i = 0; i < 4; i++) {
                            float x = quad.posByIndex(i, 0);
                            float y = quad.posByIndex(i, 1);
                            float z = quad.posByIndex(i, 2);
                            if (epsComp(x, corner.pX(this.box)) && epsComp(y, corner.pY(this.box))
                                    && epsComp(z, corner.pZ(this.box))) {
                                Vector3i vec = Direction.values()[hoz].getDirectionVec();
                                x -= vec.getX() * this.thickness;
                                y -= vec.getY() * this.thickness;
                                z -= vec.getZ() * this.thickness;
                                quad.pos(i, x, y, z);
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    public enum Corner {

        MIN_X_MIN_Y_MIN_Z(field_11060, field_11060, field_11060), MIN_X_MIN_Y_MAX_Z(field_11060, field_11060, field_11056),
        MIN_X_MAX_Y_MIN_Z(field_11060, field_11056, field_11060), MIN_X_MAX_Y_MAX_Z(field_11060, field_11056, field_11056),

        MAX_X_MIN_Y_MIN_Z(field_11056, field_11060, field_11060), MAX_X_MIN_Y_MAX_Z(field_11056, field_11060, field_11056),
        MAX_X_MAX_Y_MIN_Z(field_11056, field_11056, field_11060), MAX_X_MAX_Y_MAX_Z(field_11056, field_11056, field_11056);

        private AxisDirection xAxis;
        private AxisDirection yAxis;
        private AxisDirection zAxis;

        private static final int[] sideMask = { 0, 2, 0, 1, 0, 4 };

        Corner(AxisDirection xAxis, AxisDirection yAxis, AxisDirection zAxis) {
            this.xAxis = xAxis;
            this.yAxis = yAxis;
            this.zAxis = zAxis;
        }

        /**
         * Used to find what corner is at the 3 sides. This method assumes you pass in the X axis side, Y axis side, and
         * Z axis side, it will NOT complain about an invalid side, you will just get garbage data. This method also
         * does not care what order the 3 axes are in.
         *
         * @param sideA Side one.
         * @param sideB Side two.
         * @param sideC Side three.
         * @return The corner at the 3 sides.
         */
        public static Corner fromSides(int sideA, int sideB, int sideC) {
            // <3 Chicken-Bones.
            return values()[sideMask[sideA] | sideMask[sideB] | sideMask[sideC]];
        }

        public float pX(AxisAlignedBB box) {
            return (float) (this.xAxis == field_11060 ? box.minX : box.maxX);
        }

        public float pY(AxisAlignedBB box) {
            return (float) (this.yAxis == field_11060 ? box.minY : box.maxY);
        }

        public float pZ(AxisAlignedBB box) {
            return (float) (this.zAxis == field_11060 ? box.minZ : box.maxZ);
        }
    }

    // Should be small enough.
    private final static double EPSILON = 0.00001;

    private static boolean epsComp(float a, float b) {
        if (a == b) {
            return true;
        } else {
            return Math.abs(a - b) < EPSILON;
        }
    }

}
