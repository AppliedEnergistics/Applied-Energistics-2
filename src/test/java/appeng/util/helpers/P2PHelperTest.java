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


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import appeng.api.util.AEColor;


public class P2PHelperTest
{

	private P2PHelper unitUnderTest = new P2PHelper();

	private static final short WHITE_FREQUENCY = 0;
	private static final AEColor[] WHITE_COLORS = new AEColor[] { AEColor.WHITE, AEColor.WHITE, AEColor.WHITE, AEColor.WHITE };

	private static final short BLACK_FREQUENCY = (short) 0xFFFF;
	private static final AEColor[] BLACK_COLORS = new AEColor[] { AEColor.BLACK, AEColor.BLACK, AEColor.BLACK, AEColor.BLACK };

	private static final short MULTI_FREQUENCY = (short) 0xE8D1;
	private static final AEColor[] MULTI_COLORS = new AEColor[] { AEColor.RED, AEColor.LIGHT_GRAY, AEColor.GREEN, AEColor.ORANGE };

	private static final String HEX_WHITE_FREQUENCY = "0000";
	private static final String HEX_BLACK_FREQUENCY = "FFFF";
	private static final String HEX_MULTI_FREQUENCY = "E8D1";
	private static final String HEX_MIN_FREQUENCY = "8000";
	private static final String HEX_MAX_FREQUENCY = "7FFF";

	private static final String COLOR_STRING_WHITE_FREQUENCY = "§00§00§00§00";
	private static final String COLOR_STRING_BLACK_FREQUENCY = "§fF§fF§fF§fF";
	private static final String COLOR_STRING_MULTI_FREQUENCY = "§eE§88§dD§11";
	private static final String COLOR_STRING_MIN_FREQUENCY = "§88§00§00§00";
	private static final String COLOR_STRING_MAX_FREQUENCY = "§77§fF§fF§fF";

	@Test
	public void testToColors()
	{
		assertArrayEquals( WHITE_COLORS, unitUnderTest.toColors( WHITE_FREQUENCY ) );
		assertArrayEquals( BLACK_COLORS, unitUnderTest.toColors( BLACK_FREQUENCY ) );
		assertArrayEquals( MULTI_COLORS, unitUnderTest.toColors( MULTI_FREQUENCY ) );
	}

	@Test
	public void testFromColors()
	{
		assertEquals( WHITE_FREQUENCY, unitUnderTest.fromColors( WHITE_COLORS ) );
		assertEquals( BLACK_FREQUENCY, unitUnderTest.fromColors( BLACK_COLORS ) );
		assertEquals( MULTI_FREQUENCY, unitUnderTest.fromColors( MULTI_COLORS ) );
	}

	@Test
	public void testToAndFromColors()
	{
		for( short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++ )
		{
			assertEquals( i, unitUnderTest.fromColors( unitUnderTest.toColors( i ) ) );
		}
	}

	@Test
	public void testToHexDigit()
	{
		assertEquals( "0", unitUnderTest.toHexDigit( AEColor.WHITE ) );
		assertEquals( "1", unitUnderTest.toHexDigit( AEColor.ORANGE ) );
		assertEquals( "2", unitUnderTest.toHexDigit( AEColor.MAGENTA ) );
		assertEquals( "3", unitUnderTest.toHexDigit( AEColor.LIGHT_BLUE ) );
		assertEquals( "4", unitUnderTest.toHexDigit( AEColor.YELLOW ) );
		assertEquals( "5", unitUnderTest.toHexDigit( AEColor.LIME ) );
		assertEquals( "6", unitUnderTest.toHexDigit( AEColor.PINK ) );
		assertEquals( "7", unitUnderTest.toHexDigit( AEColor.GRAY ) );
		assertEquals( "8", unitUnderTest.toHexDigit( AEColor.LIGHT_GRAY ) );
		assertEquals( "9", unitUnderTest.toHexDigit( AEColor.CYAN ) );
		assertEquals( "A", unitUnderTest.toHexDigit( AEColor.PURPLE ) );
		assertEquals( "B", unitUnderTest.toHexDigit( AEColor.BLUE ) );
		assertEquals( "C", unitUnderTest.toHexDigit( AEColor.BROWN ) );
		assertEquals( "D", unitUnderTest.toHexDigit( AEColor.GREEN ) );
		assertEquals( "E", unitUnderTest.toHexDigit( AEColor.RED ) );
		assertEquals( "F", unitUnderTest.toHexDigit( AEColor.BLACK ) );
	}

	@Test
	public void testToHexString()
	{
		assertEquals( HEX_WHITE_FREQUENCY, unitUnderTest.toHexString( WHITE_FREQUENCY ) );
		assertEquals( HEX_BLACK_FREQUENCY, unitUnderTest.toHexString( BLACK_FREQUENCY ) );
		assertEquals( HEX_MULTI_FREQUENCY, unitUnderTest.toHexString( MULTI_FREQUENCY ) );

		assertEquals( HEX_MIN_FREQUENCY, unitUnderTest.toHexString( Short.MIN_VALUE ) );
		assertEquals( HEX_MAX_FREQUENCY, unitUnderTest.toHexString( Short.MAX_VALUE ) );
	}

	@Test
	public void testToColorHexString()
	{
		assertEquals( COLOR_STRING_WHITE_FREQUENCY, unitUnderTest.toColorHexString( WHITE_FREQUENCY ) );
		assertEquals( COLOR_STRING_BLACK_FREQUENCY, unitUnderTest.toColorHexString( BLACK_FREQUENCY ) );
		assertEquals( COLOR_STRING_MULTI_FREQUENCY, unitUnderTest.toColorHexString( MULTI_FREQUENCY ) );
		assertEquals( COLOR_STRING_MIN_FREQUENCY, unitUnderTest.toColorHexString( Short.MIN_VALUE ) );
		assertEquals( COLOR_STRING_MAX_FREQUENCY, unitUnderTest.toColorHexString( Short.MAX_VALUE ) );
	}

}
