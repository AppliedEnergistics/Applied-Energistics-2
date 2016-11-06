package appeng.integration.abstraction;


import appeng.integration.IIntegrationModule;


/**
 * Abstracts access to the JEI API functionality.
 */
public interface IJEI extends IIntegrationModule
{

	default String getSearchText()
	{
		return "";
	}

	default void setSearchText( String searchText )
	{
	}

	class Stub extends IIntegrationModule.Stub implements IJEI
	{
	}
}
