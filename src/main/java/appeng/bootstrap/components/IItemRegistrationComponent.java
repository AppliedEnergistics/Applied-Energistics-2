package appeng.bootstrap.components;


import appeng.bootstrap.IBootstrapComponent;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;


@FunctionalInterface
public interface IItemRegistrationComponent extends IBootstrapComponent {
    void itemRegistration(Side side, IForgeRegistry<Item> itemRegistry);
}
