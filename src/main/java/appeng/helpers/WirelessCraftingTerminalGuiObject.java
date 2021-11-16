package appeng.helpers;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.features.IWirelessTerminalHandler;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;

public class WirelessCraftingTerminalGuiObject extends WirelessTerminalGuiObject
        implements ISegmentedInventory, InternalInventoryHost {// TODO maybe implement InternalInventoryHost elsewhere?
    private final AppEngInternalInventory craftingGrid = new AppEngInternalInventory(this, 9);
    private final Player player;

    public WirelessCraftingTerminalGuiObject(IWirelessTerminalHandler wh, ItemStack is, Player ep, int inventorySlot) {
        super(wh, is, ep, inventorySlot);
        player = ep;
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
    public void saveChanges() {// TODO save the content of the crafting grid

    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot, ItemStack removedStack, ItemStack newStack) {

    }

    @Override
    public boolean isRemote() {
        return !(player instanceof ServerPlayer);
    }
}
