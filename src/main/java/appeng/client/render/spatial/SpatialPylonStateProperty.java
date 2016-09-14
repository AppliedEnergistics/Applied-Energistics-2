package appeng.client.render.spatial;


import net.minecraftforge.common.property.IUnlistedProperty;


/**
 * Models the rendering state of the spatial pylon, which is largely determined by the state of neighboring tiles.
 */
public class SpatialPylonStateProperty implements IUnlistedProperty<Integer>
{

	@Override
	public String getName()
	{
		return "spatial_state";
	}

	@Override
	public boolean isValid( Integer value )
	{
		int val = value;
		// The lower 6 bits are used
		return ( val & ~0x3F ) == 0;
	}

	@Override
	public Class<Integer> getType()
	{
		return Integer.class;
	}

	@Override
	public String valueToString( Integer value )
	{
		return value.toString();
	}
}
