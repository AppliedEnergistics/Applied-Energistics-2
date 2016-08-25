package appeng.bootstrap;


import java.util.function.Consumer;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import appeng.core.features.AEFeature;
import appeng.core.features.ItemDefinition;


/**
 * Allows an item to be defined and registered with the game.
 * The item is only registered once build is called.
 */
public interface IItemBuilder
{

	IItemBuilder preInit( Consumer<Item> callback );

	IItemBuilder init( Consumer<Item> callback );

	IItemBuilder postInit( Consumer<Item> callback );

	IItemBuilder features( AEFeature... features );

	IItemBuilder addFeatures( AEFeature... features );

	IItemBuilder creativeTab( CreativeTabs tab );

	IItemBuilder rendering( ItemRenderingCustomizer callback );

	ItemDefinition build();
}
