package appeng.items.tools.powered;

import java.util.function.DoubleSupplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.phys.BlockHitResult;

import appeng.helpers.WirelessCraftingTerminalMenuHost;
import appeng.menu.locator.ItemMenuHostLocator;
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
    public WirelessCraftingTerminalMenuHost<?> getMenuHost(Player player, ItemMenuHostLocator locator,
            @Nullable BlockHitResult hitResult) {
        return new WirelessCraftingTerminalMenuHost<>(this, player, locator,
                (p, sm) -> openFromInventory(p, locator, true));
    }
}
