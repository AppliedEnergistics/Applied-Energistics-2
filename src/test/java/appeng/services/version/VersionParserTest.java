package appeng.services.version;


import org.junit.Test;

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
	public void testParseGitHub_shouldPass()
	{
		assertTrue( this.parser.parse( GITHUB_VERSION ).equals( VERSION ) );
	}

	@Test( expected = AssertionError.class )
	public void parseGH_InvalidRevision_NotPass()
	{
		assertFalse( this.parser.parse( GITHUB_INVALID_REVISION ).equals( VERSION ) );
	}

	@Test( expected = AssertionError.class )
	public void parseGH_InvalidChannel_NotPass()
	{
		assertFalse( this.parser.parse( GITHUB_INVALID_CHANNEL ).equals( VERSION ) );
	}

	@Test( expected = AssertionError.class )
	public void parseGH_InvalidBuild_NotPass()
	{
		assertFalse( this.parser.parse( GITHUB_INVALID_BUILD ).equals( VERSION ) );
	}

	@Test
	public void testParseMod_shouldPass()
	{
		assertTrue( this.parser.parse( MOD_VERSION ).equals( VERSION ) );
	}

	@Test( expected = AssertionError.class )
	public void parseMod_InvalidRevision_NotPass()
	{
		assertFalse( this.parser.parse( MOD_INVALID_REVISION ).equals( VERSION ) );
	}

	@Test( expected = AssertionError.class )
	public void parseMod_InvalidChannel_NotPass()
	{
		assertFalse( this.parser.parse( MOD_INVALID_CHANNEL ).equals( VERSION ) );
	}

	@Test( expected = AssertionError.class )
	public void parseMod_InvalidBuild_NotPass()
	{
		assertFalse( this.parser.parse( MOD_INVALID_BUILD ).equals( VERSION ) );
	}
}
