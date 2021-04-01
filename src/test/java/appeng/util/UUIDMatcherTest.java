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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import UUIDMatcher;
import org.junit.Test;

/**
 * Tests for {@link UUIDMatcher}
 */
public final class UUIDMatcherTest {
    private static final String IS_UUID = "03ba29a1-d6bd-32ba-90b2-375e4d65abc9";
    private static final String NO_UUID = "no";
    private static final String INVALID_UUID = "g3ba29a1-d6bd-32ba-90b2-375e4d65abc9";

    private final UUIDMatcher matcher;

    public UUIDMatcherTest() {
        this.matcher = new UUIDMatcher();
    }

    @Test
    public void testUUID_shouldPass() {
        assertTrue(this.matcher.isUUID(IS_UUID));
    }

    @Test
    public void testNoUUD_shouldPass() {
        assertFalse(this.matcher.isUUID(NO_UUID));
    }

    @Test
    public void testInvalidUUID_shouldPass() {
        assertFalse(this.matcher.isUUID(INVALID_UUID));
    }
}
