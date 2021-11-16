package appeng.items.tools.powered;

import java.util.function.DoubleSupplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.helpers.WirelessCraftingTerminalGuiObject;
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
    public IGuiItemObject getGuiObject(ItemStack is, int playerInventorySlot, Player player, @Nullable BlockPos pos) {
        return new WirelessCraftingTerminalGuiObject(TERMINAL_HANDLER, is, player, playerInventorySlot);
    }
}
