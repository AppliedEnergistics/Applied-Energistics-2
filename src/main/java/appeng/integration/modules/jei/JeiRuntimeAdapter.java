package appeng.integration.modules.jei;


import com.google.common.base.Strings;

import mezz.jei.api.IJeiRuntime;

import appeng.integration.abstraction.IJEI;


class JeiRuntimeAdapter implements IJEI
{

	private final IJeiRuntime runtime;

	JeiRuntimeAdapter( IJeiRuntime jeiRuntime )
	{
		this.runtime = jeiRuntime;
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public String getSearchText()
	{
		return Strings.nullToEmpty( runtime.getItemListOverlay().getFilterText() );
	}

	@Override
	public void setSearchText( String searchText )
	{
		runtime.getItemListOverlay().setFilterText( Strings.nullToEmpty( searchText ) );
	}
}
