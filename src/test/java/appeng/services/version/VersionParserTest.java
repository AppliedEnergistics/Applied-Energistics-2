package appeng.services.version;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


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

	private static final DefaultVersion VERSION = new DefaultVersion( 2, Channel.Beta, 8 );

	private final VersionParser parser;

	public VersionParserTest()
	{
		this.parser = new VersionParser();
	}

	@Test
	public void testSameParsedGitHub()
	{
		final Version version = this.parser.parse( GITHUB_VERSION );

		assertEquals( version, version );
	}

	@Test
	public void testParseGitHub()
	{
		assertTrue( this.parser.parse( GITHUB_VERSION ).equals( VERSION ) );
	}

	@Test( expected = AssertionError.class )
	public void parseGH_InvalidRevision()
	{
		assertFalse( this.parser.parse( GITHUB_INVALID_REVISION ).equals( VERSION ) );
	}

	@Test( expected = AssertionError.class )
	public void parseGH_InvalidChannel()
	{
		assertFalse( this.parser.parse( GITHUB_INVALID_CHANNEL ).equals( VERSION ) );
	}

	@Test( expected = AssertionError.class )
	public void parseGH_InvalidBuild()
	{
		assertFalse( this.parser.parse( GITHUB_INVALID_BUILD ).equals( VERSION ) );
	}

	@Test
	public void testParseMod()
	{
		assertTrue( this.parser.parse( MOD_VERSION ).equals( VERSION ) );
	}

	@Test( expected = AssertionError.class )
	public void parseMod_InvalidRevision()
	{
		assertFalse( this.parser.parse( MOD_INVALID_REVISION ).equals( VERSION ) );
	}

	@Test( expected = AssertionError.class )
	public void parseMod_InvalidChannel()
	{
		assertFalse( this.parser.parse( MOD_INVALID_CHANNEL ).equals( VERSION ) );
	}

	@Test( expected = AssertionError.class )
	public void parseMod_InvalidBuild()
	{
		assertFalse( this.parser.parse( MOD_INVALID_BUILD ).equals( VERSION ) );
	}
}
