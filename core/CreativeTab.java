package appeng.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.util.Platform;

public final class CreativeTab extends CreativeTabs
{

	public static CreativeTab instance = null;

	public CreativeTab() {
		super( "appliedenergistics2" );
		if ( Platform.isClient() )
			AELog.localization( "gui", "itemGroup." + getTabLabel() );
	}

	@Override
	public ItemStack getIconItemStack()
	{
		return AEApi.instance().items().itemWirelessTerminal.stack( 1 );
	}

	public static void init()
	{
		instance = new CreativeTab();
	}

}