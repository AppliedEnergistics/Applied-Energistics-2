package appeng.integration.modules;


import appeng.helpers.Reflected;
import appeng.integration.IIntegrationModule;
import appeng.integration.abstraction.IJEI;
import appeng.integration.modules.jei.NullJEI;


public class JEI implements IIntegrationModule
{

	@Reflected
	public static JEI instance;

	private IJEI jei = new NullJEI();

	@Override
	public void init() throws Throwable
	{

	}

	@Override
	public void postInit()
	{

	}

	public void setJei( IJEI jei )
	{
		this.jei = jei;
	}

	public IJEI getJei()
	{
		return jei;
	}

}
