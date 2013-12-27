package appeng.core.features.registries;

import java.util.HashMap;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import appeng.api.config.TunnelType;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.util.Platform;

public class P2PTunnelRegistry implements IP2PTunnelRegistry
{

	HashMap<ItemStack, TunnelType> Tunnels = new HashMap();

	@Override
	public void addNewAttunement(ItemStack trigger, TunnelType type)
	{
		if ( type == null )
			throw new RuntimeException( "Invalid Tunnel Type." );

		Tunnels.put( trigger, type );
	}

	@Override
	public TunnelType getTunnelTypeByItem(ItemStack trigger)
	{
		if ( FluidContainerRegistry.isContainer( trigger ) )
			return TunnelType.FLUID;

		for (ItemStack is : Tunnels.keySet())
		{
			if ( Platform.isSameItemType( is, trigger ) )
				return Tunnels.get( is );
		}

		return null;
	}

}
