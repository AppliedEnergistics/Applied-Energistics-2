package appeng.helpers;

import java.util.function.BiConsumer;

import appeng.items.tools.powered.WirelessCraftingTerminalItem;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;

public class WirelessCraftingTerminalMenuHost<T extends WirelessCraftingTerminalItem> extends WirelessTerminalMenuHost<T>
        implements ISegmentedInventory, InternalInventoryHost {
    private final AppEngInternalInventory craftingGrid = new AppEngInternalInventory(this, 9);

    public WirelessCraftingTerminalMenuHost(T item, Player player, ItemMenuHostLocator locator,
            BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(item, player, locator, returnToMainMenu);
        craftingGrid.readFromNBT(getItemStack().getOrCreateTag(), "craftingGrid");
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(CraftingTerminalPart.INV_CRAFTING)) {
            return craftingGrid;
        } else {
            return null;
        }
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        craftingGrid.writeToNBT(getItemStack().getOrCreateTag(), "craftingGrid");
    }

}
