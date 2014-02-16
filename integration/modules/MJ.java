package appeng.integration.modules;

import appeng.integration.IIntegrationModule;
import appeng.integration.abstraction.IMJ;
import appeng.integration.abstraction.helpers.BaseMJperdition;
import appeng.integration.modules.helpers.MJPerdition;
import appeng.tile.powersink.BuildCraft;
import buildcraft.api.power.IPowerReceptor;

public class MJ implements IIntegrationModule, IMJ
{

	public static MJ instance;

	@Override
	public BaseMJperdition createPerdition(BuildCraft buildCraft)
	{
		if ( buildCraft instanceof IPowerReceptor )
			return new MJPerdition( buildCraft );
		return null;
	}

	@Override
	public void Init() throws Throwable
	{
		if ( ((Object) this) instanceof MJPerdition )
		{

		}
	}

	@Override
	public void PostInit() throws Throwable
	{

	}

}
