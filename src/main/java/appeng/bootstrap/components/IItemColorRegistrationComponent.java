package appeng.bootstrap.components;

import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;

import appeng.bootstrap.IBootstrapComponent;

public interface IItemColorRegistrationComponent extends IBootstrapComponent {

    void register(ItemColors itemColors, BlockColors blockColors);

}
