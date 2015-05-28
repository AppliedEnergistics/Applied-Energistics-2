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

package appeng.core.worlddata;


import org.junit.Assert;
import org.junit.Test;


/**
 * Tests for {@link MeteorDataNameEncoder}
 *
 * @author thatsIch
 * @version rv3 - 06.06.2015
 * @since rv3 06.06.2015
 */
public class MeteorDataNameEncoderTest
{
	private static final int WITHOUT_DIMENSION = -5;
	private static final int WITHOUT_CHUNK_X = 0;
	private static final int WITHOUT_CHUNK_Z = 13;
	private static final String WITHOUT_EXPECTED = "-5_0_13.dat";

	private static final int WITH_DIMENSION = 3;
	private static final int WITH_CHUNK_X = 32;
	private static final int WITH_CHUNK_Z = -64;
	private static final String WITH_EXPECTED = "3_2_-4.dat";

	private final MeteorDataNameEncoder encoderWithZeroShifting = new MeteorDataNameEncoder( 0 );
	private final MeteorDataNameEncoder encoderWithFourShifting = new MeteorDataNameEncoder( 4 );

	@Test
	public void testEncoderWithoutShifting()
	{
		final String expected = WITHOUT_EXPECTED;
		final String actual = this.encoderWithZeroShifting.encode( WITHOUT_DIMENSION, WITHOUT_CHUNK_X, WITHOUT_CHUNK_Z );

		Assert.assertEquals( expected, actual );
	}

	@Test
	public void testEncoderWithShifting()
	{
		final String expected = WITH_EXPECTED;
		final String actual = this.encoderWithFourShifting.encode( WITH_DIMENSION, WITH_CHUNK_X, WITH_CHUNK_Z );

		Assert.assertEquals( expected, actual );
	}
}
