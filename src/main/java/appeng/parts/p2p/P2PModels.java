package appeng.parts.p2p;


import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.ResourceLocation;

import appeng.core.AppEng;


/**
 * Helper for maintaining the models used for a variant of the P2P bus.
 */
class P2PModels
{

	public static final ResourceLocation MODEL_STATUS_OFF = new ResourceLocation( AppEng.MOD_ID, "part/p2p/p2p_tunnel_status_off" );
	public static final ResourceLocation MODEL_STATUS_ON = new ResourceLocation( AppEng.MOD_ID, "part/p2p/p2p_tunnel_status_on" );
	public static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = new ResourceLocation( AppEng.MOD_ID, "part/p2p/p2p_tunnel_status_has_channel" );

	private final List<ResourceLocation> modelsOff;
	private final List<ResourceLocation> modelsOn;
	private final List<ResourceLocation> modelsHasChannel;

	public P2PModels( String frontModelPath )
	{
		ResourceLocation frontModel = new ResourceLocation( AppEng.MOD_ID, frontModelPath );
		modelsOff = ImmutableList.of( MODEL_STATUS_OFF, frontModel );
		modelsOn = ImmutableList.of( MODEL_STATUS_ON, frontModel );
		modelsHasChannel = ImmutableList.of( MODEL_STATUS_HAS_CHANNEL, frontModel );
	}

	public List<ResourceLocation> getModel( boolean hasPower, boolean hasChannel )
	{
		if( hasPower && hasChannel )
		{
			return modelsHasChannel;
		}
		else if( hasPower )
		{
			return modelsOn;
		}
		else
		{
			return modelsOff;
		}
	}

	public List<ResourceLocation> getModels() {
		List<ResourceLocation> result = new ArrayList<>();
		result.addAll( modelsOff );
		result.addAll( modelsOn );
		result.addAll( modelsHasChannel );
		return result;
	}

}
