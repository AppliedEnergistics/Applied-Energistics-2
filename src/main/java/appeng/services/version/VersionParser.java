package appeng.services.version;


import java.security.InvalidParameterException;
import java.util.Scanner;
import java.util.regex.Pattern;


/**
 * can parse a version in form of rv2-beta-8 or rv2.beta.8
 */
public final class VersionParser
{
	private static final Pattern PATTERN_DOT = Pattern.compile( "\\." );
	private static final Pattern PATTERN_DASH = Pattern.compile( "-" );
	private static final Pattern PATTERN_REVISION = Pattern.compile( "[^0-9]+" );
	private static final Pattern PATTERN_BUILD = Pattern.compile( "[^0-9]+" );
	private static final Pattern PATTERN_NATURAL = Pattern.compile( "[0-9]+" );
	private static final Pattern PATTERN_VALID_REVISION = Pattern.compile( "^rv\\d+$" );

	/**
	 * Parses the {@link Version} out of a String
	 *
	 * @param raw String in form of rv2-beta-8 or rv2.beta.8
	 *
	 * @return {@link Version} encoded in the raw String
	 *
	 * @throws AssertionError if raw String does not match pattern of a {@link Version}
	 */
	public Version parse( String raw )
	{
		final String transformed = this.transformDelimiter( raw );
		final String[] split = transformed.split( "_" );

		return this.parseVersion( split );
	}

	/**
	 * Replaces all "." and "-" into "_" to make them uniform
	 *
	 * @param raw raw version string containing "." or "-"
	 *
	 * @return transformed raw, where "." and "-" are replaced by "_"
	 */
	private String transformDelimiter( String raw )
	{
		assert raw.contains( "." ) || raw.contains( "-" );

		final String withoutDot = PATTERN_DOT.matcher( raw ).replaceAll( "_" );
		final String withoutDash = PATTERN_DASH.matcher( withoutDot ).replaceAll( "_" );

		return withoutDash;
	}

	/**
	 * parses the {@link Version} out of the split.
	 * The split must have a length of 3,
	 * representing revision, channel and build.
	 *
	 * @param splitRaw raw version split with length of 3
	 *
	 * @return {@link Version} represented by the splitRaw
	 */
	private Version parseVersion( String[] splitRaw )
	{
		assert splitRaw.length == 3;

		final String rawRevision = splitRaw[0];
		final String rawChannel = splitRaw[1];
		final String rawBuild = splitRaw[2];

		final int revision = this.parseRevision( rawRevision );
		final Channel channel = this.parseChannel( rawChannel );
		final int build = this.parseBuild( rawBuild );

		return new DefaultVersion( revision, channel, build );
	}

	/**
	 * A revision starts with the keyword "rv", followed by a natural number
	 *
	 * @param rawRevision String containing the revision number
	 *
	 * @return revision number
	 */
	private int parseRevision( String rawRevision )
	{
		assert PATTERN_VALID_REVISION.matcher( rawRevision ).matches();

		final int revision = new Scanner( rawRevision ).useDelimiter( PATTERN_REVISION ).nextInt();

		return revision;
	}

	/**
	 * A channel is atm either one of {@link Channel#Alpha}, {@link Channel#Beta} or {@link Channel#Release}
	 *
	 * @param rawChannel String containing the channel
	 *
	 * @return matching {@link Channel} to the String
	 */
	private Channel parseChannel( String rawChannel )
	{
		assert rawChannel.equalsIgnoreCase( Channel.Alpha.name() ) || rawChannel.equalsIgnoreCase( Channel.Beta.name() ) || rawChannel.equalsIgnoreCase( Channel.Release.name() );

		for( Channel channel : Channel.values() )
		{
			if( channel.name().equalsIgnoreCase( rawChannel ) )
			{
				return channel;
			}
		}

		throw new InvalidParameterException( "Raw channel did not contain any of the pre-programmed types." );
	}

	/**
	 * A build is just a natural number
	 *
	 * @param rawBuild String containing the build number
	 *
	 * @return build number
	 */
	private int parseBuild( String rawBuild )
	{
		assert PATTERN_NATURAL.matcher( rawBuild ).matches();

		final int build = new Scanner( rawBuild ).useDelimiter( PATTERN_BUILD ).nextInt();

		return build;
	}
}
