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

package appeng.parts.automation;

import java.util.ArrayList;
import java.util.List;

/**
 * Models in which directions - looking at the front face - a plane (annihilation, formation, etc.) is connected to
 * other planes of the same type.
 */
public final class PlaneConnections {

    private final boolean up;
    private final boolean right;
    private final boolean down;
    private final boolean left;

    private static final int BITMASK_UP = 8;
    private static final int BITMASK_RIGHT = 4;
    private static final int BITMASK_DOWN = 2;
    private static final int BITMASK_LEFT = 1;

    public static final List<PlaneConnections> PERMUTATIONS = generatePermutations();

    private static List<PlaneConnections> generatePermutations() {
        List<PlaneConnections> connections = new ArrayList<>(16);

        for (int i = 0; i < 16; i++) {
            boolean up = (i & BITMASK_UP) != 0;
            boolean right = (i & BITMASK_RIGHT) != 0;
            boolean down = (i & BITMASK_DOWN) != 0;
            boolean left = (i & BITMASK_LEFT) != 0;

            connections.add(new PlaneConnections(up, right, down, left));
        }

        return connections;
    }

    private PlaneConnections(boolean up, boolean right, boolean down, boolean left) {
        this.up = up;
        this.right = right;
        this.down = down;
        this.left = left;
    }

    public static PlaneConnections of(boolean up, boolean right, boolean down, boolean left) {
        return PERMUTATIONS.get(getIndex(up, right, down, left));
    }

    public boolean isUp() {
        return this.up;
    }

    public boolean isRight() {
        return this.right;
    }

    public boolean isDown() {
        return this.down;
    }

    public boolean isLeft() {
        return this.left;
    }

    // The combination of connections expressed as a number ranging from [0,15]
    public int getIndex() {
        return getIndex(this.up, this.right, this.down, this.left);
    }

    private static int getIndex(boolean up, boolean right, boolean down, boolean left) {
        return (up ? BITMASK_UP : 0) + (right ? BITMASK_RIGHT : 0) + (left ? BITMASK_LEFT : 0)
                + (down ? BITMASK_DOWN : 0);
    }

    @Override
    public boolean equals(Object o) {
        // This class is final/has a private constructor, and is interned
        return this == o;
    }

    @Override
    public int hashCode() {
        int result = (this.up ? 1 : 0);
        result = 31 * result + (this.right ? 1 : 0);
        result = 31 * result + (this.down ? 1 : 0);
        result = 31 * result + (this.left ? 1 : 0);
        return result;
    }
}
