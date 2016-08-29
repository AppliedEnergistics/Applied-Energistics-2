package appeng.client.render.cablebus;


import net.minecraft.util.ResourceLocation;

import appeng.api.util.AEColor;
import appeng.core.AppEng;


/**
 * AE can render the core of a cable (the core that connections are made to, in case the cable is not a straight line)
 * in three different ways:
 * - Glass
 * - Covered (also used by the Smart Cable)
 * - Dense
 */
public enum CableCoreType
{
	GLASS( "parts/cable/core/glass" ),
	COVERED( "parts/cable/core/covered" ),
	DENSE( "parts/cable/core/dense" );

	private final String textureFolder;

	CableCoreType( String textureFolder )
	{
		this.textureFolder = textureFolder;
	}

	public ResourceLocation getTexture( AEColor color )
	{
		return new ResourceLocation( AppEng.MOD_ID, textureFolder + "/" + color.name().toLowerCase() );
	}

}
