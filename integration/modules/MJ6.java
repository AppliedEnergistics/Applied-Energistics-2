package appeng.integration.modules;

import appeng.integration.BaseModule;
import appeng.integration.abstraction.IMJ6;
import buildcraft.api.mj.IBatteryObject;

public class MJ6 extends BaseModule implements IMJ6
{

	public static MJ6 instance;

	public MJ6() {
		TestClass( IBatteryObject.class );
	}

	@Override
	public void Init() throws Throwable
	{
	}

	@Override
	public void PostInit() throws Throwable
	{
	}

}
