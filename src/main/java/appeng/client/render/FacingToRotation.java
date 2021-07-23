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

package appeng.client.render;

import java.util.Locale;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.math.vector.Vector4f;

/**
 * TODO: Removed useless stuff.
 */
public enum FacingToRotation implements IStringSerializable {

    // DUNSWE
    // @formatter:off
    DOWN_DOWN(new Vector3f(0, 0, 0)), // NOOP
    DOWN_UP(new Vector3f(0, 0, 0)), // NOOP
    DOWN_NORTH(new Vector3f(-90, 0, 0)), DOWN_SOUTH(new Vector3f(-90, 0, 180)), DOWN_WEST(new Vector3f(-90, 0, 90)),
    DOWN_EAST(new Vector3f(-90, 0, -90)), UP_DOWN(new Vector3f(0, 0, 0)), // NOOP
    UP_UP(new Vector3f(0, 0, 0)), // NOOP
    UP_NORTH(new Vector3f(90, 0, 180)), UP_SOUTH(new Vector3f(90, 0, 0)), UP_WEST(new Vector3f(90, 0, 90)),
    UP_EAST(new Vector3f(90, 0, -90)), NORTH_DOWN(new Vector3f(0, 0, 180)), NORTH_UP(new Vector3f(0, 0, 0)),
    NORTH_NORTH(new Vector3f(0, 0, 0)), // NOOP
    NORTH_SOUTH(new Vector3f(0, 0, 0)), // NOOP
    NORTH_WEST(new Vector3f(0, 0, 90)), NORTH_EAST(new Vector3f(0, 0, -90)), SOUTH_DOWN(new Vector3f(0, 180, 180)),
    SOUTH_UP(new Vector3f(0, 180, 0)), SOUTH_NORTH(new Vector3f(0, 0, 0)), // NOOP
    SOUTH_SOUTH(new Vector3f(0, 0, 0)), // NOOP
    SOUTH_WEST(new Vector3f(0, 180, -90)), SOUTH_EAST(new Vector3f(0, 180, 90)), WEST_DOWN(new Vector3f(0, 90, 180)),
    WEST_UP(new Vector3f(0, 90, 0)), WEST_NORTH(new Vector3f(0, 90, -90)), WEST_SOUTH(new Vector3f(0, 90, 90)),
    WEST_WEST(new Vector3f(0, 0, 0)), // NOOP
    WEST_EAST(new Vector3f(0, 0, 0)), // NOOP
    EAST_DOWN(new Vector3f(0, -90, 180)), EAST_UP(new Vector3f(0, -90, 0)), EAST_NORTH(new Vector3f(0, -90, 90)),
    EAST_SOUTH(new Vector3f(0, -90, -90)), EAST_WEST(new Vector3f(0, 0, 0)), // NOOP
    EAST_EAST(new Vector3f(0, 0, 0)); // NOOP
    // @formatter:on

    private final Vector3f rot;
    private final Quaternion xRot;
    private final Quaternion yRot;
    private final Quaternion zRot;
    private final Matrix4f mat;

    FacingToRotation(Vector3f rot) {
        this.rot = rot;
        this.mat = new Matrix4f();
        this.mat.setIdentity();
        this.mat.multiply(xRot = Vector3f.XP.rotationDegrees(rot.x()));
        this.mat.multiply(yRot = Vector3f.YP.rotationDegrees(rot.y()));
        this.mat.multiply(zRot = Vector3f.ZP.rotationDegrees(rot.z()));
    }

    public boolean isRedundant() {
        return rot.x() == 0 && rot.y() == 0 && rot.z() == 0;
    }

    public Vector3f getRot() {
        return this.rot;
    }

    public Matrix4f getMat() {
        return new Matrix4f(this.mat);
    }

    public void push(MatrixStack mStack) {
        mStack.mulPose(xRot);
        mStack.mulPose(yRot);
        mStack.mulPose(zRot);
    }

    public Direction rotate(Direction facing) {
        Vector3i dir = facing.getNormal();
        Vector4f vec = new Vector4f(dir.getX(), dir.getY(), dir.getZ(), 1);
        vec.transform(mat);
        return Direction.getNearest(vec.x(), vec.y(), vec.z());
    }

    public Direction resultingRotate(Direction facing) {
        for (Direction face : Direction.values()) {
            if (this.rotate(face) == facing) {
                return face;
            }
        }
        return null;
    }

    public static FacingToRotation get(Direction forward, Direction up) {
        return values()[forward.ordinal() * 6 + up.ordinal()];
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
