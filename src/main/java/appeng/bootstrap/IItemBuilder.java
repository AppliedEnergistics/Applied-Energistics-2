package appeng.bootstrap;


import net.minecraft.creativetab.CreativeTabs;

import appeng.core.features.AEFeature;
import appeng.core.features.ItemDefinition;
import appeng.util.Platform;


/**
 * Allows an item to be defined and registered with the game.
 * The item is only registered once build is called.
 */
public interface IItemBuilder
{

	IItemBuilder features( AEFeature... features );

	IItemBuilder addFeatures( AEFeature... features );

	IItemBuilder creativeTab( CreativeTabs tab );

	IItemBuilder rendering( ItemRenderingCustomizer callback );

	ItemDefinition build();

}
