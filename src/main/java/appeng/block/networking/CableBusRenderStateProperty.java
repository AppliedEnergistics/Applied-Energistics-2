package appeng.block.networking;


import net.minecraftforge.common.property.IUnlistedProperty;

import appeng.client.render.cablebus.CableBusRenderState;


/**
 * An unlisted property for the cable bus block's render state.
 */
public class CableBusRenderStateProperty implements IUnlistedProperty<CableBusRenderState>
{
	@Override
	public String getName()
	{
		return "cable_bus_render_state";
	}

	@Override
	public boolean isValid( CableBusRenderState value )
	{
		return value != null;
	}

	@Override
	public Class<CableBusRenderState> getType()
	{
		return CableBusRenderState.class;
	}

	@Override
	public String valueToString( CableBusRenderState value )
	{
		return value.toString();
	}
}
