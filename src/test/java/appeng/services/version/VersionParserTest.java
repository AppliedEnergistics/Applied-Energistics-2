package appeng.services.version;


import appeng.services.version.exceptions.*;
import org.junit.Test;

import static org.junit.Assert.*;


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
