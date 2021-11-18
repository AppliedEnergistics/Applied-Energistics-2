package appeng.items.tools.powered;

import java.util.function.DoubleSupplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.guiobjects.ItemMenuHost;
import appeng.helpers.WirelessCraftingTerminalMenuHost;
import appeng.menu.me.items.WirelessCraftingTermMenu;

public class WirelessCraftingTerminalItem extends WirelessTerminalItem {
    public WirelessCraftingTerminalItem(final DoubleSupplier powerCapacity, Properties props) {
        super(powerCapacity, props);
    }

    @Override
    public MenuType<?> getMenuType() {
        return WirelessCraftingTermMenu.TYPE;
    }

    @Nullable
    @Override
    public ItemMenuHost getGuiObject(Player player, int inventorySlot, ItemStack stack, @Nullable BlockPos pos) {
        return new WirelessCraftingTerminalMenuHost(player, inventorySlot, stack);
    }
}
