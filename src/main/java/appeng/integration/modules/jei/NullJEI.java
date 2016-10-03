package appeng.integration.modules.jei;


import appeng.integration.abstraction.IJEI;


public class NullJEI implements IJEI
{
	@Override
	public boolean isEnabled()
	{
		return false;
	}

	@Override
	public String getSearchText()
	{
		return "";
	}

	@Override
	public void setSearchText( String searchText )
	{
	}
}
