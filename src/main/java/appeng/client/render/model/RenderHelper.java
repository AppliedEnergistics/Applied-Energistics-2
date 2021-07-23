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

import java.util.EnumMap;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

// TODO: Investigate use of CubeBuilder instead
final class RenderHelper {

    private static EnumMap<net.minecraft.core.Direction, List<Vec3>> cornersForFacing = generateCornersForFacings();

    private RenderHelper() {

    }

    static List<Vec3> getFaceCorners(Direction side) {
        return cornersForFacing.get(side);
    }

    private static EnumMap<Direction, List<Vec3>> generateCornersForFacings() {
        EnumMap<Direction, List<Vec3>> result = new EnumMap<>(Direction.class);

        for (net.minecraft.core.Direction facing : Direction.values()) {
            List<Vec3> corners;

            float offset = facing.getAxisDirection() == AxisDirection.NEGATIVE ? 0 : 1;

            switch (facing.getAxis()) {
                default:
                case X:
                    corners = Lists.newArrayList(new Vec3(offset, 1, 1), new Vec3(offset, 0, 1),
                            new Vec3(offset, 0, 0), new Vec3(offset, 1, 0));
                    break;
                case Y:
                    corners = Lists.newArrayList(new Vec3(1, offset, 1), new Vec3(1, offset, 0),
                            new Vec3(0, offset, 0), new Vec3(0, offset, 1));
                    break;
                case Z:
                    corners = Lists.newArrayList(new Vec3(0, 1, offset), new Vec3(0, 0, offset),
                            new Vec3(1, 0, offset), new Vec3(1, 1, offset));
                    break;
            }

            if (facing.getAxisDirection() == AxisDirection.NEGATIVE) {
                corners = Lists.reverse(corners);
            }

            result.put(facing, ImmutableList.copyOf(corners));
        }

        return result;
    }

    private static Vec3 adjust(Vec3 vec, Axis axis, double delta) {
        switch (axis) {
            default:
            case X:
                return new Vec3(vec.x + delta, vec.y, vec.z);
            case Y:
                return new Vec3(vec.x, vec.y + delta, vec.z);
            case Z:
                return new Vec3(vec.x, vec.y, vec.z + delta);
        }
    }
}
