
package appeng.bootstrap.components;

import net.minecraft.item.Item;
import net.fabricmc.api.EnvType;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.bootstrap.IBootstrapComponent;

@FunctionalInterface
public interface IItemRegistrationComponent extends IBootstrapComponent {
    void itemRegistration(EnvType dist, IForgeRegistry<Item> itemRegistry);
}
