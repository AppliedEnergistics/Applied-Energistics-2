package appeng.bootstrap.components;

import appeng.bootstrap.IBootstrapComponent;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;

public interface IItemColorRegistrationComponent extends IBootstrapComponent {

    void register(ItemColors itemColors, BlockColors blockColors);

}
