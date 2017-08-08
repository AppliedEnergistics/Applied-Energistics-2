
package appeng.bootstrap.components;


import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.bootstrap.IBootstrapComponent;


@FunctionalInterface
public interface IItemRegistrationComponent extends IBootstrapComponent
{
	void itemRegistration( Side side, IForgeRegistry<Item> itemRegistry );
}
