package appeng.integration.modules;

import powercrystals.minefactoryreloaded.api.rednet.RedNetConnectionType;
import appeng.integration.BaseModule;

public class MFR extends BaseModule
{

	public static MFR instance;

	public MFR() {
		TestClass( RedNetConnectionType.class );
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
