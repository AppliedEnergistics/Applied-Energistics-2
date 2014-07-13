package appeng.integration.modules;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.integration.BaseModule;
import appeng.integration.IIntegrationModule;
import cpw.mods.fml.common.registry.GameRegistry;

public class RF extends BaseModule implements IIntegrationModule
{

	public static RF instance;

	public RF() {
		TestClass( cofh.api.energy.IEnergyHandler.class );
		TestClass( cofh.api.energy.IEnergyConnection.class );
	}

	@Override
	public void Init()
	{
		IP2PTunnelRegistry reg = AEApi.instance().registries().p2pTunnel();

		ItemStack energyPipe = GameRegistry.findItemStack( "ExtraUtilities", "extractor_base", 1 );
		if ( energyPipe != null )
		{
			energyPipe.setItemDamage( 12 );
			reg.addNewAttunement( energyPipe, TunnelType.RF_POWER );
		}

		ItemStack energyConduit = GameRegistry.findItemStack( "EnderIO", "itemPowerConduit", 1 );
		if ( energyConduit != null )
		{
			energyConduit.setItemDamage( OreDictionary.WILDCARD_VALUE );
			reg.addNewAttunement( energyConduit, TunnelType.RF_POWER );
		}

		reg.addNewAttunement( GameRegistry.findItemStack( "EnderIO", "blockCapacitorBank", 1 ), TunnelType.RF_POWER );
		reg.addNewAttunement( GameRegistry.findItemStack( "EnderIO", "blockPowerMonitor", 1 ), TunnelType.RF_POWER );
	}

	@Override
	public void PostInit()
	{

	}

}
