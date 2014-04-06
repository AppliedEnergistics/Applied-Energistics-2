package appeng.integration.modules;

import appeng.integration.BaseModule;
import appeng.integration.abstraction.IMJ;
import appeng.integration.modules.helpers.MJPerdition;
import appeng.tile.powersink.MinecraftJoules;
import buildcraft.api.power.IPowerReceptor;

public class MJ extends BaseModule implements IMJ
{

	public static MJ instance;

	public MJ() {
		TestClass( IPowerReceptor.class );
	}

	@Override
	public Object createPerdition(MinecraftJoules buildCraft)
	{
		if ( buildCraft instanceof IPowerReceptor )
			return new MJPerdition( buildCraft );
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
