package appeng.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.items.parts.ItemFacade;
import appeng.util.Platform;

public final class CreativeTabFacade extends CreativeTabs
{

	public static CreativeTabFacade instance = null;

	public CreativeTabFacade() {
		super( "appliedenergistics2.facades" );
		if ( Platform.isClient() )
			AELog.localization( "gui", "itemGroup." + getTabLabel() );
	}

	@Override
	public ItemStack getIconItemStack()
	{
		return ((ItemFacade) AEApi.instance().items().itemFacade.item()).getCreativeTabIcon();
	}

	public static void init()
	{
		instance = new CreativeTabFacade();
	}

}