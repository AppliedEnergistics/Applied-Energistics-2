package appeng.bootstrap.components;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;

import appeng.bootstrap.IBootstrapComponent;

public interface IItemColorRegistrationComponent extends IBootstrapComponent {

    void register(ItemColors itemColors, BlockColors blockColors);

}
