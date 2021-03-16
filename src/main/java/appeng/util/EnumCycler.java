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

import java.util.EnumSet;

/**
 * Simple utility class to help with select the "next" or "previous" value in a list of options represented by an
 * enumeration.
 */
public final class EnumCycler {

    private EnumCycler() {
    }

    public static <T extends Enum<T>> T rotateEnum(T ce, final boolean backwards, final EnumSet<T> validOptions) {
        do {
            if (backwards) {
                ce = prevEnum(ce);
            } else {
                ce = next(ce);
            }
        } while (!validOptions.contains(ce));

        return ce;
    }

    /*
     * Simple way to cycle an enum...
     */
    public static <T extends Enum<T>> T prevEnum(final T ce) {
        T[] values = ce.getDeclaringClass().getEnumConstants();

        int pLoc = ce.ordinal() - 1;
        if (pLoc < 0) {
            pLoc = values.length - 1;
        }

        if (pLoc < 0 || pLoc >= values.length) {
            pLoc = 0;
        }

        return values[pLoc];
    }

    /*
     * Simple way to cycle an enum...
     */
    public static <T extends Enum<T>> T next(final T ce) {
        T[] values = ce.getDeclaringClass().getEnumConstants();

        int pLoc = ce.ordinal() + 1;
        if (pLoc >= values.length) {
            pLoc = 0;
        }

        if (pLoc < 0 || pLoc >= values.length) {
            pLoc = 0;
        }

        return values[pLoc];
    }

}
