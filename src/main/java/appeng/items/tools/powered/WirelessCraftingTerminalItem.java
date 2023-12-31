package appeng.items.tools.powered;

import java.util.function.DoubleSupplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.helpers.WirelessCraftingTerminalMenuHost;
import appeng.menu.locator.MenuItemLocator;
import appeng.menu.me.items.WirelessCraftingTermMenu;

public class WirelessCraftingTerminalItem extends WirelessTerminalItem {
    public WirelessCraftingTerminalItem(DoubleSupplier powerCapacity, Properties props) {
        super(powerCapacity, props);
    }

    @Override
    public MenuType<?> getMenuType() {
        return WirelessCraftingTermMenu.TYPE;
    }

    @Nullable
    @Override
    public ItemMenuHost getMenuHost(Player player, MenuItemLocator locator, ItemStack stack, @Nullable BlockPos pos) {
        return new WirelessCraftingTerminalMenuHost(player, locator, stack,
                (p, sm) -> openFromInventory(p, locator, true));
    }
}
