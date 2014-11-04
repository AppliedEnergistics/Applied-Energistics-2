package appeng.integration.modules;

import appeng.integration.BaseModule;

public class RotaryCraft extends BaseModule
{

	public static RotaryCraft instance;

	public RotaryCraft() {
		TestClass( Reika.RotaryCraft.API.ShaftPowerReceiver.class );
	}

	@Override
	public void Init() throws Throwable
	{

	}

	@Override
	public void PostInit()
	{

	}

}
