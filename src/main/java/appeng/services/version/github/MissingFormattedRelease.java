
package appeng.services.version.github;


import appeng.services.version.MissingVersion;
import appeng.services.version.Version;


/**
 * Exceptional template, when no meaningful {@link FormattedRelease} could be obtained
 */
public final class MissingFormattedRelease implements FormattedRelease
{
	private final Version version;

	public MissingFormattedRelease()
	{
		this.version = new MissingVersion();
	}

	/**
	 * @return empty string
	 */
	@Override
	public String changelog()
	{
		return "";
	}

	/**
	 * @return {@link MissingVersion}
	 */
	@Override
	public Version version()
	{
		return this.version;
	}
}
