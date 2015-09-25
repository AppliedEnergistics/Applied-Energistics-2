
package appeng.services.version.github;


import appeng.services.version.Version;


/**
 * Default template when a {@link FormattedRelease} is needed.
 */
public final class DefaultFormattedRelease implements FormattedRelease
{
	private final Version version;
	private final String changelog;

	public DefaultFormattedRelease( final Version version, final String changelog )
	{
		this.version = version;
		this.changelog = changelog;
	}

	@Override
	public String changelog()
	{
		return this.changelog;
	}

	@Override
	public Version version()
	{
		return this.version;
	}
}
