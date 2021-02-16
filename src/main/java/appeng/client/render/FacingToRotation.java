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

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vector4f;

/**
 * TODO: Removed useless stuff.
 */
public enum FacingToRotation implements StringIdentifiable {

    // DUNSWE
    // @formatter:off
    DOWN_DOWN(new Vec3f(0, 0, 0)), // NOOP
    DOWN_UP(new Vec3f(0, 0, 0)), // NOOP
    DOWN_NORTH(new Vec3f(-90, 0, 0)), DOWN_SOUTH(new Vec3f(-90, 0, 180)), DOWN_WEST(new Vec3f(-90, 0, 90)),
    DOWN_EAST(new Vec3f(-90, 0, -90)), UP_DOWN(new Vec3f(0, 0, 0)), // NOOP
    UP_UP(new Vec3f(0, 0, 0)), // NOOP
    UP_NORTH(new Vec3f(90, 0, 180)), UP_SOUTH(new Vec3f(90, 0, 0)), UP_WEST(new Vec3f(90, 0, 90)),
    UP_EAST(new Vec3f(90, 0, -90)), NORTH_DOWN(new Vec3f(0, 0, 180)), NORTH_UP(new Vec3f(0, 0, 0)),
    NORTH_NORTH(new Vec3f(0, 0, 0)), // NOOP
    NORTH_SOUTH(new Vec3f(0, 0, 0)), // NOOP
    NORTH_WEST(new Vec3f(0, 0, 90)), NORTH_EAST(new Vec3f(0, 0, -90)), SOUTH_DOWN(new Vec3f(0, 180, 180)),
    SOUTH_UP(new Vec3f(0, 180, 0)), SOUTH_NORTH(new Vec3f(0, 0, 0)), // NOOP
    SOUTH_SOUTH(new Vec3f(0, 0, 0)), // NOOP
    SOUTH_WEST(new Vec3f(0, 180, -90)), SOUTH_EAST(new Vec3f(0, 180, 90)), WEST_DOWN(new Vec3f(0, 90, 180)),
    WEST_UP(new Vec3f(0, 90, 0)), WEST_NORTH(new Vec3f(0, 90, -90)), WEST_SOUTH(new Vec3f(0, 90, 90)),
    WEST_WEST(new Vec3f(0, 0, 0)), // NOOP
    WEST_EAST(new Vec3f(0, 0, 0)), // NOOP
    EAST_DOWN(new Vec3f(0, -90, 180)), EAST_UP(new Vec3f(0, -90, 0)), EAST_NORTH(new Vec3f(0, -90, 90)),
    EAST_SOUTH(new Vec3f(0, -90, -90)), EAST_WEST(new Vec3f(0, 0, 0)), // NOOP
    EAST_EAST(new Vec3f(0, 0, 0)); // NOOP
    // @formatter:on

    private final Vec3f rot;
    private final Quaternion xRot;
    private final Quaternion yRot;
    private final Quaternion zRot;
    private final Quaternion combinedRotation;
    private final Matrix4f mat;

    FacingToRotation(Vec3f rot) {
        this.rot = rot;
        this.mat = new Matrix4f();
        this.mat.loadIdentity();
        this.mat.multiply(xRot = Vec3f.POSITIVE_X.getDegreesQuaternion(rot.getX()));
        this.mat.multiply(yRot = Vec3f.POSITIVE_Y.getDegreesQuaternion(rot.getY()));
        this.mat.multiply(zRot = Vec3f.POSITIVE_Z.getDegreesQuaternion(rot.getZ()));
        this.combinedRotation = new Quaternion(rot.getX(), rot.getY(), rot.getZ(), true);
    }

    public boolean isRedundant() {
        return rot.getX() == 0 && rot.getY() == 0 && rot.getZ() == 0;
    }

    public Quaternion getRot() {
        return this.combinedRotation;
    }

    public Matrix4f getMat() {
        return new Matrix4f(this.mat);
    }

    public void push(MatrixStack mStack) {
        mStack.multiply(xRot);
        mStack.multiply(yRot);
        mStack.multiply(zRot);
    }

    public Direction rotate(Direction facing) {
        Vec3i dir = facing.getVector();
        Vector4f vec = new Vector4f(dir.getX(), dir.getY(), dir.getZ(), 1);
        vec.transform(mat);
        return Direction.getFacing(vec.getX(), vec.getY(), vec.getZ());
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
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
