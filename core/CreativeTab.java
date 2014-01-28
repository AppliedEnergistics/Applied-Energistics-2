package appeng.core;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.util.AEItemDefinition;
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
		return findFirst( AEApi.instance().blocks().blockController, AEApi.instance().blocks().blockChest, AEApi.instance().blocks().blockCellWorkbench,
				AEApi.instance().blocks().blockFluix, AEApi.instance().items().itemCell1k, AEApi.instance().items().itemNetworkTool,
				AEApi.instance().materials().materialFluixCrystal, AEApi.instance().materials().materialCertusQuartzCrystal );
	}

	private ItemStack findFirst(AEItemDefinition... choices)
	{
		for (AEItemDefinition a : choices)
		{
			ItemStack is = a.stack( 1 );
			if ( is != null )
				return is;
		}

		return new ItemStack( Block.chest );
	}

	public static void init()
	{
		instance = new CreativeTab();
	}

}