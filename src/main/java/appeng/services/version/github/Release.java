package appeng.services.version.github;


/**
 * Template class for Gson to write values from the Json Object into an actual class
 */
@SuppressWarnings( "ALL" )
public class Release
{
	/**
	 * name of the tag it is saved
	 */
	public String tag_name;

	/**
	 * Contains the changelog
	 */
	public String body;
}
