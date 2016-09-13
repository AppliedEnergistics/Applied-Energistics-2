package appeng.client.render.cablebus;


import java.util.EnumMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.util.ResourceLocation;

import appeng.api.util.AECableType;
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

	private static final Map<AECableType, CableCoreType> cableMapping = generateCableMapping();

	/**
	 * Creates the mapping that assigns a cable core type to an AE cable type.
	 */
	private static Map<AECableType, CableCoreType> generateCableMapping()
	{

		Map<AECableType, CableCoreType> result = new EnumMap<>( AECableType.class );

		result.put( AECableType.GLASS, CableCoreType.GLASS );
		result.put( AECableType.COVERED, CableCoreType.COVERED );
		result.put( AECableType.SMART, CableCoreType.COVERED );
		result.put( AECableType.DENSE, CableCoreType.DENSE );

		return ImmutableMap.copyOf( result );
	}

	private final String textureFolder;

	CableCoreType( String textureFolder )
	{
		this.textureFolder = textureFolder;
	}

	/**
	 * @return The type of core that should be rendered when the given cable isn't straight and needs to have a core to attach connections to.
	 * Is null for the NULL cable.
	 */
	public static CableCoreType fromCableType( AECableType cableType )
	{
		return cableMapping.get( cableType );
	}

	public ResourceLocation getTexture( AEColor color )
	{
		return new ResourceLocation( AppEng.MOD_ID, textureFolder + "/" + color.name().toLowerCase() );
	}

}