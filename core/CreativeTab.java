package appeng.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.util.AEItemDefinition;

public final class CreativeTab extends CreativeTabs
{

	public static CreativeTab instance = null;

	public CreativeTab() {
		super( "appliedenergistics2" );
	}

	@Override
	public Item getTabIconItem()
	{
		return getIconItemStack().getItem();
	}

	@Override
	public ItemStack getIconItemStack()
	{
		return findFirst( AEApi.instance().blocks().blockController, AEApi.instance().blocks().blockChest, AEApi.instance().blocks().blockCellWorkbench, AEApi
				.instance().blocks().blockFluix, AEApi.instance().items().itemCell1k, AEApi.instance().items().itemNetworkTool,
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

		return new ItemStack( Blocks.chest );
	}

	public static void init()
	{
		instance = new CreativeTab();
	}

}