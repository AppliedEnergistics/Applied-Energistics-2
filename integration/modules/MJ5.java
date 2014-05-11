package appeng.integration.modules;

import appeng.integration.BaseModule;
import appeng.integration.abstraction.IMJ5;
import appeng.integration.modules.helpers.MJPerdition;
import buildcraft.api.power.IPowerReceptor;

public class MJ5 extends BaseModule implements IMJ5
{

	public static MJ5 instance;

	public MJ5() {
		TestClass( IPowerReceptor.class );
	}

	@Override
	public Object createPerdition(Object buildCraft)
	{
		if ( buildCraft instanceof IPowerReceptor )
			return new MJPerdition( (IPowerReceptor) buildCraft );
		return null;
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
