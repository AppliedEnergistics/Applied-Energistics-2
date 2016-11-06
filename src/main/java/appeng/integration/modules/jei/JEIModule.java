package appeng.integration.modules.jei;


import appeng.integration.abstraction.IJEI;


public class JEIModule implements IJEI
{

	private IJEI jei = new IJEI.Stub();

	public void setJei( IJEI jei )
	{
		this.jei = jei;
	}

	public IJEI getJei()
	{
		return jei;
	}

	@Override
	public String getSearchText()
	{
		return jei.getSearchText();
	}

	@Override
	public void setSearchText( String searchText )
	{
		jei.setSearchText( searchText );
	}

	@Override
	public boolean isEnabled()
	{
		return jei.isEnabled();
	}

}
