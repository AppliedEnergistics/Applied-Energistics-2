package appeng.integration.modules;

import appeng.api.util.AEColor;
import appeng.integration.BaseModule;
import appeng.integration.abstraction.ICLApi;

public class CLApi extends BaseModule implements ICLApi
{

	public static CLApi instance;

	@Override
	public void Init() throws Throwable
	{
		TestClass( coloredlightscore.src.api.CLApi.class );
	}

	@Override
	public void PostInit()
	{
		// :P
	}

	@Override
	public int colorLight(AEColor color, int light)
	{
		int mv = color.mediumVariant;

		float r = (mv >> 16) & 0xff;
		float g = (mv >> 8) & 0xff;
		float b = (mv >> 0) & 0xff;

		return coloredlightscore.src.api.CLApi.makeRGBLightValue( r / 255.0f, g / 255.0f, b / 255.0f, light / 15.0f );
	}
}
