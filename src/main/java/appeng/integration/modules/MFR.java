package appeng.integration.modules;

import powercrystals.minefactoryreloaded.api.rednet.connectivity.IRedNetConnection;
import appeng.integration.BaseModule;

public class MFR extends BaseModule
{

	public static MFR instance;

	public MFR() {
		TestClass( IRedNetConnection.class );
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
