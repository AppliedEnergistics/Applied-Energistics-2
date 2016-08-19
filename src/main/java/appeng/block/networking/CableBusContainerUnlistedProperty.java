package appeng.block.networking;

import net.minecraftforge.common.property.IUnlistedProperty;

import appeng.parts.CableBusContainer;

public class CableBusContainerUnlistedProperty implements IUnlistedProperty<CableBusContainer>
{

	@Override
	public String getName()
	{
		return "bus";
	}

	@Override
	public boolean isValid( CableBusContainer value )
	{
		return true;
	}

	@Override
	public Class<CableBusContainer> getType()
	{
		return CableBusContainer.class;
	}

	@Override
	public String valueToString( CableBusContainer value )
	{
		return null;
	}

}
