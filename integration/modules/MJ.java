package appeng.integration.modules;

import appeng.integration.BaseModule;
import appeng.integration.abstraction.IMJ;
import appeng.integration.abstraction.helpers.BaseMJperdition;
import appeng.integration.modules.helpers.MJPerdition;
import appeng.tile.powersink.BuildCraft;
import buildcraft.api.power.IPowerReceptor;

public class MJ extends BaseModule implements IMJ
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
		TestClass( MJPerdition.class );
	}

	@Override
	public void PostInit() throws Throwable
	{

	}

}
