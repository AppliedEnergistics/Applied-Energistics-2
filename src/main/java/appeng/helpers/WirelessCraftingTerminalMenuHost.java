package appeng.helpers;

import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;

public class WirelessCraftingTerminalMenuHost extends WirelessTerminalMenuHost
        implements ISegmentedInventory, InternalInventoryHost {
    private final AppEngInternalInventory craftingGrid = new AppEngInternalInventory(this, 9);

    public WirelessCraftingTerminalMenuHost(Player player, @Nullable ItemMenuHostLocator locator, ItemStack itemStack,
            BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(player, locator, itemStack, returnToMainMenu);
        craftingGrid.readFromNBT(getItemStack().getOrCreateTag(), "craftingGrid");
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(CraftingTerminalPart.INV_CRAFTING)) {
            return craftingGrid;
        } else
            return null;
    }

    @Override
    public void saveChanges() {
        craftingGrid.writeToNBT(getItemStack().getOrCreateTag(), "craftingGrid");
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {

    }

}
