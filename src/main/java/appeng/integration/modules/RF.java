package appeng.integration.modules;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.integration.BaseModule;
import cpw.mods.fml.common.registry.GameRegistry;

public class RF extends BaseModule
{

	public static RF instance;

	public RF() {
		TestClass( cofh.api.energy.IEnergyHandler.class );
		TestClass( cofh.api.energy.IEnergyConnection.class );
	}

	@Override
	public void Init()
	{
	}

	void RFStack(String mod, String name, int dmg)
	{
		ItemStack modItem = GameRegistry.findItemStack( mod, name, 1 );
		if ( modItem != null )
		{
			modItem.setItemDamage( dmg );
			AEApi.instance().registries().p2pTunnel().addNewAttunement( modItem, TunnelType.RF_POWER );
		}
	}

	@Override
	public void PostInit()
	{
		RFStack( "ExtraUtilities", "extractor_base", 12 );
		RFStack( "ExtraUtilities", "pipes", 11 );
		RFStack( "ExtraUtilities", "pipes", 14 );
		RFStack( "ExtraUtilities", "generator", OreDictionary.WILDCARD_VALUE );

		RFStack( "ThermalExpansion", "Cell", OreDictionary.WILDCARD_VALUE );
		RFStack( "ThermalExpansion", "Dynamo", OreDictionary.WILDCARD_VALUE );

		RFStack( "EnderIO", "itemPowerConduit", OreDictionary.WILDCARD_VALUE );
		RFStack( "EnderIO", "blockCapacitorBank", 0 );
		RFStack( "EnderIO", "blockPowerMonitor", 0 );
	}

}
