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

package appeng.api.orientation;

import com.mojang.math.Transformation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.EnumSet;
import java.util.Set;

/**
 * All possible rotations for a fully orientable block.
 */
public enum BlockOrientation {

    // DUNSWE
    // @formatter:off
    DOWN_NORTH(90, 0, 0, 0),
    DOWN_WEST(90, 0, 270, 1),
    DOWN_SOUTH(90, 0, 180, 2),
    DOWN_EAST(90, 0, 90, 3),

    UP_NORTH(270, 0, 180, 0),
    UP_EAST(270, 0, 90, 1),
    UP_SOUTH(270, 0, 0, 2),
    UP_WEST(270, 0, 270, 3),

    NORTH_UP(0, 0, 0, 0), // Default,
    NORTH_WEST(0, 0, 270, 1),
    NORTH_DOWN(0, 0, 180, 2),
    NORTH_EAST(0, 0, 90, 3),

    SOUTH_UP(0, 180, 0, 0),
    SOUTH_EAST(0, 180, 90, 1),
    SOUTH_DOWN(0, 180, 180, 2),
    SOUTH_WEST(0, 180, 270, 3),

    WEST_UP(0, 270, 0, 0),
    WEST_SOUTH(0, 270, 270, 1),
    WEST_DOWN(0, 270, 180, 2),
    WEST_NORTH(0, 270, 90, 3),

    EAST_UP(0, 90, 0, 0),
    EAST_NORTH(0, 90, 270, 1),
    EAST_DOWN(0, 90, 180, 2),
    EAST_SOUTH(0, 90, 90, 3);
    // @formatter:on

    private final int angleX;
    private final int angleY;
    private final int angleZ;
    private final Quaternionf quaternion;
    private final Transformation transformation;
    /**
     * How many times it has been rotated clock-wise around in 90° increments around its facing.
     */
    private final int spin;
    // Map each Direction to the Direction it'll be rotated to
    private final Direction[] rotatedSideTo;
    // Reverse of rotatedSideTo
    private final Direction[] rotatedSideFrom;

    BlockOrientation(int angleX, int angleY, int angleZ, int spin) {
        this.angleX = angleX;
        this.angleY = angleY;
        this.angleZ = angleZ;

        // NOTE: Mojangs block model rotation rotates in the opposite direction
        quaternion = new Quaternionf().rotateYXZ(
                -angleY * Mth.DEG_TO_RAD,
                -angleX * Mth.DEG_TO_RAD,
                -angleZ * Mth.DEG_TO_RAD);

        if (angleX == 0 && angleY == 0 && angleZ == 0) {
            this.transformation = Transformation.identity();
        } else {
            var rotationMatrix = new Matrix4f()
                    .identity()
                    .rotate(quaternion);
            this.transformation = new Transformation(rotationMatrix);
        }
        this.spin = spin;

        // Build a mapping between the sides in this orientation
        this.rotatedSideTo = new Direction[Direction.values().length];
        this.rotatedSideFrom = new Direction[Direction.values().length];
        for (var direction : Direction.values()) {
            var normal = direction.step();
            normal.rotate(quaternion);
            var rotatedTo = Direction.getNearest(normal.x(), normal.y(), normal.z());
            rotatedSideTo[direction.ordinal()] = rotatedTo;
            rotatedSideFrom[rotatedTo.ordinal()] = direction;
        }
    }

    /**
     * Changes the orientation of the given block entity to this, if possible.
     */
    public void setOn(BlockEntity be) {
        setOn(be.getLevel(), be.getBlockPos());
    }

    /**
     * Changes the orientation of the block at the given position/level to this,
     * if possible.
     */
    public void setOn(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        var strategy = IOrientationStrategy.get(state);
        var newState = strategy.setOrientation(state,
                getSide(RelativeSide.FRONT),
                getSpin());
        if (newState != state) {
            level.setBlockAndUpdate(pos, newState);
        }

    }

    public boolean isRedundant() {
        return angleX == 0 && angleY == 0 && angleZ == 0;
    }

    public Quaternionf getQuaternion() {
        return this.quaternion;
    }

    public Transformation getTransformation() {
        return transformation;
    }

    public Direction rotate(Direction facing) {
        return rotatedSideTo[facing.ordinal()];
    }

    public Direction resultingRotate(Direction facing) {
        return rotatedSideFrom[facing.ordinal()];
    }

    public int getAngleX() {
        return angleX;
    }

    public int getAngleY() {
        return angleY;
    }

    public int getAngleZ() {
        return angleZ;
    }

    public int getSpin() {
        return spin;
    }

    public static BlockOrientation get(Direction facing) {
        return get(facing, 0);
    }

    /**
     * Gets the block orientation in which the blocks front and top are facing the specified directions.
     */
    public static BlockOrientation get(Direction front, Direction top) {
        var offset = front.ordinal() * 4;
        for (var i = offset; i < offset + 4; i++) {
            var orientation = values()[i];
            if (orientation.getSide(RelativeSide.TOP) == top) {
                return orientation;
            }
        }
        return values()[offset]; // Degenerated up -> return default
    }

    public static BlockOrientation get(Direction facing, int spin) {
        return values()[facing.ordinal() * 4 + spin];
    }

    public static BlockOrientation get(BlockEntity blockEntity) {
        var blockState = blockEntity.getBlockState();
        return get(blockState);
    }

    public static BlockOrientation get(BlockState state) {
        var strategy = IOrientationStrategy.get(state);
        return get(strategy, state);
    }

    public static BlockOrientation get(IOrientationStrategy strategy, BlockState state) {
        var facing = strategy.getFacing(state);
        var spin = strategy.getSpin(state);
        return get(facing, spin);
    }

    public Direction getSide(RelativeSide side) {
        return rotate(side.getUnrotatedSide());
    }

    public RelativeSide getRelativeSide(Direction side) {
        return RelativeSide.fromUnrotatedSide(resultingRotate(side));
    }

    public Set<Direction> getSides(Set<RelativeSide> relativeSides) {
        var result = EnumSet.noneOf(Direction.class);
        for (var relativeSide : relativeSides) {
            result.add(getSide(relativeSide));
        }
        return result;
    }

    public Set<RelativeSide> getRelativeSides(Set<Direction> sides) {
        var result = EnumSet.noneOf(RelativeSide.class);
        for (var side : sides) {
            result.add(getRelativeSide(side));
        }
        return result;
    }

    public BlockOrientation rotateClockwiseAround(Direction side) {
        return rotateClockwiseAround(side.getAxis(), side.getAxisDirection());
    }

    public BlockOrientation rotateClockwiseAround(Direction.Axis axis, Direction.AxisDirection direction) {
        var facing = getSide(RelativeSide.FRONT);
        var up = getSide(RelativeSide.TOP);
        Direction newFacing;
        Direction newUp;
        if (direction == Direction.AxisDirection.POSITIVE) {
            newFacing = facing.getClockWise(axis);
            newUp = up.getClockWise(axis);
        } else {
            newFacing = facing.getCounterClockWise(axis);
            newUp = up.getCounterClockWise(axis);
        }
        return get(newFacing, newUp);
    }
}
