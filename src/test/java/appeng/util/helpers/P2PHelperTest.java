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

	private static short WHITE_FREQUENCY = 0;
	private static AEColor[] WHITE_COLORS = new AEColor[] { AEColor.WHITE, AEColor.WHITE, AEColor.WHITE, AEColor.WHITE };

	private static short BLACK_FREQUENCY = (short) 0xFFFF;
	private static AEColor[] BLACK_COLORS = new AEColor[] { AEColor.BLACK, AEColor.BLACK, AEColor.BLACK, AEColor.BLACK };

	private static short MULTI_FREQUENCY = (short) 0xE8D1;
	private static AEColor[] MULTI_COLORS = new AEColor[] { AEColor.RED, AEColor.LIGHT_GRAY, AEColor.GREEN, AEColor.ORANGE };

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

}
