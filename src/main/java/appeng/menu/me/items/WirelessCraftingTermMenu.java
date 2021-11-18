package appeng.menu.me.items;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.SecurityPermissions;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.networking.IGridNode;
import appeng.helpers.WirelessCraftingTerminalMenuHost;
import appeng.menu.implementations.MenuTypeBuilder;

/**
 * Can only be used with a host that implements {@link ISegmentedInventory} and exposes an inventory named "crafting" to
 * store the crafting grid and output.
 *
 * @see appeng.client.gui.me.items.CraftingTermScreen
 */
public class WirelessCraftingTermMenu extends CraftingTermMenu {

    public static final MenuType<WirelessCraftingTermMenu> TYPE = MenuTypeBuilder
            .create(WirelessCraftingTermMenu::new, WirelessCraftingTerminalMenuHost.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("wirelesscraftingterm");

    private final WirelessCraftingTerminalMenuHost guiObject;

    public WirelessCraftingTermMenu(int id, Inventory ip, WirelessCraftingTerminalMenuHost monitorable) {
        super(TYPE, id, ip, monitorable, false);
        this.createPlayerInventorySlots(ip);
        this.guiObject = monitorable;
    }

    public IGridNode getNetworkNode() {
        return guiObject.getActionableNode();
    }
}
