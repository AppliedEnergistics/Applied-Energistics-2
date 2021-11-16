package appeng.menu.me.items;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.SecurityPermissions;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.networking.IGridNode;
import appeng.helpers.WirelessCraftingTerminalGuiObject;
import appeng.menu.implementations.MenuTypeBuilder;

/**
 * Can only be used with a host that implements {@link ISegmentedInventory} and exposes an inventory named "crafting" to
 * store the crafting grid and output.
 *
 * @see appeng.client.gui.me.items.CraftingTermScreen
 */
public class WirelessCraftingTermMenu extends CraftingTermMenu {

    public static final MenuType<WirelessCraftingTermMenu> TYPE = MenuTypeBuilder
            .create(WirelessCraftingTermMenu::new, WirelessCraftingTerminalGuiObject.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("wirelesscraftingterm");

    private final WirelessCraftingTerminalGuiObject guiObject;

    public WirelessCraftingTermMenu(int id, final Inventory ip, final WirelessCraftingTerminalGuiObject monitorable) {
        super(TYPE, id, ip, monitorable, false);
        this.lockPlayerInventorySlot(monitorable.getInventorySlot());
        this.createPlayerInventorySlots(ip);
        this.guiObject = monitorable;
    }

    @Override
    public void broadcastChanges() {
        if (checkGuiItemNotInSlot())
            return;

        checkWirelessRange();
        updateItemPowerStatus();
        super.broadcastChanges();
    }

    public IGridNode getNetworkNode() {
        return guiObject.getActionableNode();
    }
}
