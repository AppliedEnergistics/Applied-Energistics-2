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

package appeng.services.version;


import org.junit.Assert;
import org.junit.Test;


/**
 * Tests for {@link Version}
 *
 * @author thatsIch
 * @version rv3 - 16.05.2015
 * @since rv3 16.05.2015
 */
public final class VersionTest
{
	private static final Version DEFAULT_VERSION_RV2_BETA_8 = new DefaultVersion( 2, Channel.Beta, 8 );
	private static final Version DEFAULT_VERSION_RV2_BETA_9 = new DefaultVersion( 2, Channel.Beta, 9 );
	private static final Version DEFAULT_VERSION_RV3_BETA_8 = new DefaultVersion( 3, Channel.Beta, 8 );
	private static final Version DEFAULT_VERSION_RV2_ALPHA_8 = new DefaultVersion( 2, Channel.Alpha, 8 );
	private static final Version DO_NOT_CHECK_VERSION = new DoNotCheckVersion();
	private static final Version MISSING_VERSION = new MissingVersion();

	@Test
	public void testDevBuild()
	{
		Assert.assertEquals( DO_NOT_CHECK_VERSION.formatted(), "dev build" );
	}

	@Test
	public void testMissingBuild()
	{
		Assert.assertEquals( MISSING_VERSION.formatted(), "missing" );
	}

	@Test
	public void compareVersionToDoNotCheck()
	{
		Assert.assertFalse( DEFAULT_VERSION_RV2_ALPHA_8.isNewerAs( DO_NOT_CHECK_VERSION ) );
		Assert.assertTrue( DO_NOT_CHECK_VERSION.isNewerAs( DEFAULT_VERSION_RV2_ALPHA_8 ) );
	}

	@Test
	public void compareVersionToMissingVersion()
	{
		Assert.assertTrue( DEFAULT_VERSION_RV2_ALPHA_8.isNewerAs( MISSING_VERSION ) );
		Assert.assertFalse( MISSING_VERSION.isNewerAs( DEFAULT_VERSION_RV2_ALPHA_8 ) );
	}

	@Test
	public void compareTwoDefaultVersions()
	{
		Assert.assertTrue( DEFAULT_VERSION_RV2_BETA_8.isNewerAs( DEFAULT_VERSION_RV2_ALPHA_8 ) );
	}

	@Test
	public void testEqualsNonVersion()
	{
		Assert.assertFalse( DEFAULT_VERSION_RV2_ALPHA_8.equals( new Object() ) );
	}

	@Test
	public void testEqualsUnequalBuild()
	{
		Assert.assertFalse( DEFAULT_VERSION_RV2_BETA_8.equals( DEFAULT_VERSION_RV2_BETA_9 ) );
	}

	@Test
	public void testEqualsUnequalChannel()
	{
		Assert.assertFalse( DEFAULT_VERSION_RV2_BETA_8.equals( DEFAULT_VERSION_RV2_ALPHA_8 ) );
	}

	@Test
	public void testEqualsUnequalRevision()
	{
		Assert.assertFalse( DEFAULT_VERSION_RV2_BETA_8.equals( DEFAULT_VERSION_RV3_BETA_8 ) );
	}

	@Test
	public void testUnequalHash()
	{
		Assert.assertNotEquals( DEFAULT_VERSION_RV2_BETA_8.hashCode(), DEFAULT_VERSION_RV2_ALPHA_8.hashCode() );
	}

	@Test
	public void testToString()
	{
		Assert.assertEquals( DEFAULT_VERSION_RV2_BETA_8.toString(), "Version{revision=2, channel=Beta, build=8}" );
	}

	@Test
	public void testFormatted()
	{
		Assert.assertEquals( DEFAULT_VERSION_RV2_BETA_8.formatted(), "rv2-beta-8" );
	}

}
