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

package appeng.util.helpers;

import appeng.api.util.AEColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class P2PHelperTest {

    private P2PHelper unitUnderTest = new P2PHelper();

    private static final short WHITE_FREQUENCY = 0;
    private static final AEColor[] WHITE_COLORS = new AEColor[] { AEColor.WHITE, AEColor.WHITE, AEColor.WHITE,
            AEColor.WHITE };

    private static final short BLACK_FREQUENCY = (short) 0xFFFF;
    private static final AEColor[] BLACK_COLORS = new AEColor[] { AEColor.BLACK, AEColor.BLACK, AEColor.BLACK,
            AEColor.BLACK };

    private static final short MULTI_FREQUENCY = (short) 0xE8D1;
    private static final AEColor[] MULTI_COLORS = new AEColor[] { AEColor.RED, AEColor.LIGHT_GRAY, AEColor.GREEN,
            AEColor.ORANGE };

    private static final String HEX_WHITE_FREQUENCY = "0000";
    private static final String HEX_BLACK_FREQUENCY = "FFFF";
    private static final String HEX_MULTI_FREQUENCY = "E8D1";
    private static final String HEX_MIN_FREQUENCY = "8000";
    private static final String HEX_MAX_FREQUENCY = "7FFF";

    @Test
    public void testToColors() {
        assertArrayEquals(WHITE_COLORS, this.unitUnderTest.toColors(WHITE_FREQUENCY));
        assertArrayEquals(BLACK_COLORS, this.unitUnderTest.toColors(BLACK_FREQUENCY));
        assertArrayEquals(MULTI_COLORS, this.unitUnderTest.toColors(MULTI_FREQUENCY));
    }

    @Test
    public void testFromColors() {
        assertEquals(WHITE_FREQUENCY, this.unitUnderTest.fromColors(WHITE_COLORS));
        assertEquals(BLACK_FREQUENCY, this.unitUnderTest.fromColors(BLACK_COLORS));
        assertEquals(MULTI_FREQUENCY, this.unitUnderTest.fromColors(MULTI_COLORS));
    }

    @Test
    public void testToAndFromColors() {
        for (short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++) {
            assertEquals(i, this.unitUnderTest.fromColors(this.unitUnderTest.toColors(i)));
        }
    }

    @Test
    public void testToHexDigit() {
        assertEquals("0", this.unitUnderTest.toHexDigit(AEColor.WHITE));
        assertEquals("1", this.unitUnderTest.toHexDigit(AEColor.ORANGE));
        assertEquals("2", this.unitUnderTest.toHexDigit(AEColor.MAGENTA));
        assertEquals("3", this.unitUnderTest.toHexDigit(AEColor.LIGHT_BLUE));
        assertEquals("4", this.unitUnderTest.toHexDigit(AEColor.YELLOW));
        assertEquals("5", this.unitUnderTest.toHexDigit(AEColor.LIME));
        assertEquals("6", this.unitUnderTest.toHexDigit(AEColor.PINK));
        assertEquals("7", this.unitUnderTest.toHexDigit(AEColor.GRAY));
        assertEquals("8", this.unitUnderTest.toHexDigit(AEColor.LIGHT_GRAY));
        assertEquals("9", this.unitUnderTest.toHexDigit(AEColor.CYAN));
        assertEquals("A", this.unitUnderTest.toHexDigit(AEColor.PURPLE));
        assertEquals("B", this.unitUnderTest.toHexDigit(AEColor.BLUE));
        assertEquals("C", this.unitUnderTest.toHexDigit(AEColor.BROWN));
        assertEquals("D", this.unitUnderTest.toHexDigit(AEColor.GREEN));
        assertEquals("E", this.unitUnderTest.toHexDigit(AEColor.RED));
        assertEquals("F", this.unitUnderTest.toHexDigit(AEColor.BLACK));
    }

    @Test
    public void testToHexString() {
        assertEquals(HEX_WHITE_FREQUENCY, this.unitUnderTest.toHexString(WHITE_FREQUENCY));
        assertEquals(HEX_BLACK_FREQUENCY, this.unitUnderTest.toHexString(BLACK_FREQUENCY));
        assertEquals(HEX_MULTI_FREQUENCY, this.unitUnderTest.toHexString(MULTI_FREQUENCY));

        assertEquals(HEX_MIN_FREQUENCY, this.unitUnderTest.toHexString(Short.MIN_VALUE));
        assertEquals(HEX_MAX_FREQUENCY, this.unitUnderTest.toHexString(Short.MAX_VALUE));
    }

}
