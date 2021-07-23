/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.helpers;

import net.minecraft.util.Direction;
import net.minecraft.world.IBlockReader;

import appeng.api.util.IOrientable;

public class LocationRotation implements IOrientable {

    private final IBlockReader w;
    private final int x;
    private final int y;
    private final int z;

    public LocationRotation(final IBlockReader world, final int x, final int y, final int z) {
        this.w = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public Direction getForward() {
        if (this.getUp().getStepY() == 0) {
            return Direction.UP;
        }
        return Direction.SOUTH;
    }

    @Override
    public Direction getUp() {
        final int num = Math.abs(this.x + this.y + this.z) % 6;
        return Direction.values()[num];
    }

    @Override
    public void setOrientation(final Direction forward, final Direction up) {

    }
}
