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

package appeng.util;


import java.util.regex.Pattern;


/**
 * Regex wrapper for {@link java.util.UUID}s to not rely on try catch
 */
public final class UUIDMatcher {
    /**
     * String which is the regular expression for {@link java.util.UUID}s
     */
    private static final String UUID_REGEX = "[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}";

    /**
     * Pattern which pre-compiles the {@link appeng.util.UUIDMatcher#UUID_REGEX}
     */
    private static final Pattern PATTERN = Pattern.compile(UUID_REGEX);

    /**
     * Checks if a potential {@link java.util.UUID} is an {@link java.util.UUID} by applying a regular expression on it.
     *
     * @param potential to be checked potential {@link java.util.UUID}
     * @return true, if the potential {@link java.util.UUID} is indeed an {@link java.util.UUID}
     */
    public boolean isUUID(final CharSequence potential) {
        return PATTERN.matcher(potential).matches();
    }
}
