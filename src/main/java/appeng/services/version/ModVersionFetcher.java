package appeng.services.version;


/**
 * Wrapper for {@link VersionParser} to check if the check is happening in developer environment or in a pull request.
 *
 * In that case ignore the check.
 */
public final class ModVersionFetcher implements VersionFetcher
{
	private final String rawModVersion;
	private final VersionParser parser;

	public ModVersionFetcher( final String rawModVersion, final VersionParser parser )
	{
		this.rawModVersion = rawModVersion;
		this.parser = parser;
	}

	/**
	 * Parses only, if not checked in developer environment or in a pull request
	 *
	 * @return {@link DoNotCheckVersion} if in developer environment or pull request, else the parsed {@link Version}
	 */
	@Override
	public Version get()
	{
		if( this.rawModVersion.equals( "@version@" ) || this.rawModVersion.contains( "pr" ) )
		{
			return new DoNotCheckVersion();
		}
		else
		{
			final Version version = this.parser.parse( this.rawModVersion );

			return version;
		}
	}
}
