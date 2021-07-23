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

package appeng.parts;

import java.util.List;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import appeng.api.parts.IPartCollisionHelper;
import appeng.api.util.AEPartLocation;

public class BusCollisionHelper implements IPartCollisionHelper {

    private final List<AxisAlignedBB> boxes;

    private final Direction x;
    private final Direction y;
    private final Direction z;

    private final boolean isVisual;

    public BusCollisionHelper(final List<AxisAlignedBB> boxes, final Direction x, final Direction y, final Direction z,
            final boolean visual) {
        this.boxes = boxes;
        this.x = x;
        this.y = y;
        this.z = z;
        this.isVisual = visual;
    }

    public BusCollisionHelper(final List<AxisAlignedBB> boxes, final AEPartLocation s, final boolean visual) {
        this.boxes = boxes;
        this.isVisual = visual;

        switch (s) {
            case DOWN:
                this.x = Direction.EAST;
                this.y = Direction.NORTH;
                this.z = Direction.DOWN;
                break;
            case UP:
                this.x = Direction.EAST;
                this.y = Direction.SOUTH;
                this.z = Direction.UP;
                break;
            case EAST:
                this.x = Direction.SOUTH;
                this.y = Direction.UP;
                this.z = Direction.EAST;
                break;
            case WEST:
                this.x = Direction.NORTH;
                this.y = Direction.UP;
                this.z = Direction.WEST;
                break;
            case NORTH:
                this.x = Direction.WEST;
                this.y = Direction.UP;
                this.z = Direction.NORTH;
                break;
            case SOUTH:
                this.x = Direction.EAST;
                this.y = Direction.UP;
                this.z = Direction.SOUTH;
                break;
            case INTERNAL:
            default:
                this.x = Direction.EAST;
                this.y = Direction.UP;
                this.z = Direction.SOUTH;
                break;
        }
    }

    @Override
    public void addBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        minX /= 16.0;
        minY /= 16.0;
        minZ /= 16.0;
        maxX /= 16.0;
        maxY /= 16.0;
        maxZ /= 16.0;

        double aX = minX * this.x.getStepX() + minY * this.y.getStepX() + minZ * this.z.getStepX();
        double aY = minX * this.x.getStepY() + minY * this.y.getStepY() + minZ * this.z.getStepY();
        double aZ = minX * this.x.getStepZ() + minY * this.y.getStepZ() + minZ * this.z.getStepZ();

        double bX = maxX * this.x.getStepX() + maxY * this.y.getStepX() + maxZ * this.z.getStepX();
        double bY = maxX * this.x.getStepY() + maxY * this.y.getStepY() + maxZ * this.z.getStepY();
        double bZ = maxX * this.x.getStepZ() + maxY * this.y.getStepZ() + maxZ * this.z.getStepZ();

        if (this.x.getStepX() + this.y.getStepX() + this.z.getStepX() < 0) {
            aX += 1;
            bX += 1;
        }

        if (this.x.getStepY() + this.y.getStepY() + this.z.getStepY() < 0) {
            aY += 1;
            bY += 1;
        }

        if (this.x.getStepZ() + this.y.getStepZ() + this.z.getStepZ() < 0) {
            aZ += 1;
            bZ += 1;
        }

        minX = Math.min(aX, bX);
        minY = Math.min(aY, bY);
        minZ = Math.min(aZ, bZ);
        maxX = Math.max(aX, bX);
        maxY = Math.max(aY, bY);
        maxZ = Math.max(aZ, bZ);

        this.boxes.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
    }

    @Override
    public Direction getWorldX() {
        return this.x;
    }

    @Override
    public Direction getWorldY() {
        return this.y;
    }

    @Override
    public Direction getWorldZ() {
        return this.z;
    }

    @Override
    public boolean isBBCollision() {
        return !this.isVisual;
    }
}
