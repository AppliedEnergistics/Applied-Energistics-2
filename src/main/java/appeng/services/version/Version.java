package appeng.services.version;


/**
 * Stores version information, which are easily compared
 */
public interface Version
{
	/**
	 * @return revision of this version
	 */
	int revision();

	/**
	 * @return channel of this version
	 */
	Channel channel();

	/**
	 * @return build of this version
	 */
	int build();

	/**
	 * A version is never if these criteria are met:
	 * if the current revision is higher than the compared revision OR
	 * if revision are equal and the current channel is higher than the compared channel (Release > Beta > Alpha) OR
	 * if revision, channel are equal and the build is higher than the compared build
	 *
	 * @return true if criteria are met
	 */
	boolean isNewerAs( Version maybeOlder );

	/**
	 * Prints the revision, channel and build into a common displayed way
	 *
	 * rv2-beta-8
	 *
	 * @return formatted version
	 */
	String formatted();
}
