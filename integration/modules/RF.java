package appeng.integration.modules;

import appeng.integration.BaseModule;
import appeng.integration.IIntegrationModule;

public class RF extends BaseModule implements IIntegrationModule
{

	public static RF instance;

	public RF() {
		TestClass( cofh.api.energy.IEnergyHandler.class );
	}

	@Override
	public void Init()
	{

	}

	@Override
	public void PostInit()
	{

	}

}
