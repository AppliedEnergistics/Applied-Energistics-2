package appeng.integration.abstraction;


/**
 * Abstracts access to the JEI API functionality.
 */
public interface IJEI
{

	boolean isEnabled();

	String getSearchText();

	void setSearchText( String searchText );
}
