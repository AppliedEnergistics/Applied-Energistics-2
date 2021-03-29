/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.util;

import com.google.common.base.Preconditions;

import java.util.EnumSet;

/**
 * Simple utility class to help with select the "next" or "previous" value in a list of options represented by an
 * enumeration.
 */
public final class EnumCycler {

    private EnumCycler() {
    }

    public static <T extends Enum<T>> T rotateEnum(T ce, final boolean backwards, final EnumSet<T> validOptions) {
        Preconditions.checkArgument(!validOptions.isEmpty());

        int direction = backwards ? -1 : 1;
        T[] values = ce.getDeclaringClass().getEnumConstants();

        do {
            // mod naturally cycles a changing integer on a range [0, N]
            int pLoc = Math.floorMod(ce.ordinal() + direction, values.length);
            ce = values[pLoc];
        } while (!validOptions.contains(ce));

        return ce;
    }

    public static <T extends Enum<T>> T next(final T ce) {
        return rotateEnum(ce, false, EnumSet.allOf(ce.getDeclaringClass()));
    }

    public static <T extends Enum<T>> T prev(final T ce) {
        return rotateEnum(ce, true, EnumSet.allOf(ce.getDeclaringClass()));
    }
}
