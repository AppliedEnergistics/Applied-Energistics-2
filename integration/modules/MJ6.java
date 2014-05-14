package appeng.integration.modules;

import appeng.integration.BaseModule;
import appeng.integration.abstraction.IMJ6;
import buildcraft.api.mj.IBatteryObject;
import buildcraft.api.mj.IBatteryProvider;
import buildcraft.api.mj.ISidedBatteryProvider;

public class MJ6 extends BaseModule implements IMJ6
{

	public static MJ6 instance;

	public MJ6() {
		TestClass( IBatteryObject.class );
		TestClass( IBatteryProvider.class );
		TestClass( ISidedBatteryProvider.class );
		throw new RuntimeException( "Disabled For Now!" );
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
