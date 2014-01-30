package appeng.integration.modules;

import appeng.integration.IIntegrationModule;
import appeng.integration.abstraction.IMJ;
import appeng.integration.modules.helpers.BCPerdition;
import appeng.integration.modules.helpers.BaseBCperdition;
import appeng.tile.powersink.BuildCraft;
import buildcraft.api.power.IPowerReceptor;

public class MJ implements IIntegrationModule, IMJ
{

	public static MJ instance;

	@Override
	public BaseBCperdition createPerdition(BuildCraft buildCraft)
	{
		if ( buildCraft instanceof IPowerReceptor )
			return new BCPerdition( buildCraft );
		return null;
	}

	@Override
	public void Init() throws Throwable
	{
		if ( ((Object) this) instanceof BCPerdition )
		{

		}
	}

	@Override
	public void PostInit() throws Throwable
	{
		// TODO Auto-generated method stub

	}

}
