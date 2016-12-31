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

package appeng.services.version;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import appeng.services.version.exceptions.InvalidBuildException;
import appeng.services.version.exceptions.InvalidChannelException;
import appeng.services.version.exceptions.InvalidRevisionException;
import appeng.services.version.exceptions.InvalidVersionException;
import appeng.services.version.exceptions.MissingSeparatorException;
import appeng.services.version.exceptions.VersionCheckerException;


/**
 * Tests for {@link VersionParser}
 */
public final class VersionParserTest
{
	private static final String GITHUB_VERSION = "rv2.beta.8";
	private static final String GITHUB_INVALID_REVISION = "2.beta.8";
	private static final String GITHUB_INVALID_CHANNEL = "rv2.gamma.8";
	private static final String GITHUB_INVALID_BUILD = "rv2.beta.b8";
	private static final String MOD_VERSION = "rv2-beta-8";
	private static final String MOD_INVALID_REVISION = "2-beta-8";
	private static final String MOD_INVALID_CHANNEL = "rv2-gamma-8";
	private static final String MOD_INVALID_BUILD = "rv2-beta-b8";
	private static final String GENERIC_MISSING_SEPARATOR = "foobar";
	private static final String GENERIC_INVALID_VERSION = "foo-bar";

	private static final DefaultVersion VERSION = new DefaultVersion( 2, Channel.Beta, 8 );

	private final VersionParser parser;

	public VersionParserTest()
	{
		this.parser = new VersionParser();
	}

	@Test
	public void testSameParsedGitHub() throws VersionCheckerException
	{
		final Version version = this.parser.parse( GITHUB_VERSION );

		assertEquals( version, version );
	}

	@Test
	public void testParseGitHub() throws VersionCheckerException
	{
		assertTrue( this.parser.parse( GITHUB_VERSION ).equals( VERSION ) );
	}

	@Test( expected = InvalidRevisionException.class )
	public void parseGH_InvalidRevision() throws VersionCheckerException
	{
		assertFalse( this.parser.parse( GITHUB_INVALID_REVISION ).equals( VERSION ) );
	}

	@Test( expected = InvalidChannelException.class )
	public void parseGH_InvalidChannel() throws VersionCheckerException
	{
		assertFalse( this.parser.parse( GITHUB_INVALID_CHANNEL ).equals( VERSION ) );
	}

	@Test( expected = InvalidBuildException.class )
	public void parseGH_InvalidBuild() throws VersionCheckerException
	{
		assertFalse( this.parser.parse( GITHUB_INVALID_BUILD ).equals( VERSION ) );
	}

	@Test
	public void testParseMod() throws VersionCheckerException
	{
		assertTrue( this.parser.parse( MOD_VERSION ).equals( VERSION ) );
	}

	@Test( expected = InvalidRevisionException.class )
	public void parseMod_InvalidRevision() throws VersionCheckerException
	{
		assertFalse( this.parser.parse( MOD_INVALID_REVISION ).equals( VERSION ) );
	}

	@Test( expected = InvalidChannelException.class )
	public void parseMod_InvalidChannel() throws VersionCheckerException
	{
		assertFalse( this.parser.parse( MOD_INVALID_CHANNEL ).equals( VERSION ) );
	}

	@Test( expected = InvalidBuildException.class )
	public void parseMod_InvalidBuild() throws VersionCheckerException
	{
		assertFalse( this.parser.parse( MOD_INVALID_BUILD ).equals( VERSION ) );
	}

	@Test( expected = MissingSeparatorException.class )
	public void parseGeneric_MissingSeparator() throws VersionCheckerException
	{
		assertFalse( this.parser.parse( GENERIC_MISSING_SEPARATOR ).equals( VERSION ) );
	}

	@Test( expected = InvalidVersionException.class )
	public void parseGeneric_InvalidVersion() throws VersionCheckerException
	{
		assertFalse( this.parser.parse( GENERIC_INVALID_VERSION ).equals( VERSION ) );
	}
}
