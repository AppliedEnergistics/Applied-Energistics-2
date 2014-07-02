package appeng.integration.modules;

import appeng.integration.BaseModule;
import appeng.integration.IIntegrationModule;

public class RFItem extends BaseModule implements IIntegrationModule
{

	public static RFItem instance;

	public RFItem() {
		TestClass( cofh.api.energy.IEnergyContainerItem.class );
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
