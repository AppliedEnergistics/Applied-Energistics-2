package appeng.services.version.github;


import appeng.services.version.Version;


/**
 * Represents the acquired, processed information through github about a release of Applied Energistics 2
 */
public interface FormattedRelease
{
	/**
	 * @return changelog
	 */
	String changelog();

	/**
	 * @return processed version
	 */
	Version version();
}
