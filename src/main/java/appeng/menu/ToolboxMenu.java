package appeng.menu;

import net.minecraft.network.chat.Component;

import appeng.items.contents.NetworkToolMenuHost;
import appeng.items.tools.NetworkToolItem;
import appeng.menu.slot.RestrictedInputSlot;

/**
 * Helper class for dealing with an equipped toolbox.
 */
public class ToolboxMenu {
    private final AEBaseMenu menu;
    private final NetworkToolMenuHost<?> inv;

    public ToolboxMenu(AEBaseMenu menu) {
        this.menu = menu;

        this.inv = NetworkToolItem.findNetworkToolInv(menu.getPlayer());
        if (inv != null) {
            var playerSlot = inv.getPlayerInventorySlot();
            if (playerSlot != null) {
                menu.lockPlayerInventorySlot(playerSlot);
            }

            // Add quick access slots for the upgrade cards stored in the toolbox
            var upgradeCardInv = this.inv.getInventory();
            for (int i = 0; i < upgradeCardInv.size(); i++) {
                var slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgradeCardInv, i);
                // The toolbox is in the network tool that is part of the player inventory
                menu.addSlot(slot, SlotSemantics.TOOLBOX);
            }
        }
    }

    public boolean isPresent() {
        return this.inv != null;
    }

    public void tick() {
        if (inv != null) {
            if (!inv.isValid()) {
                menu.setValidMenu(false);
                return;
            }
            inv.tick();
        }
    }

    public Component getName() {
        return this.inv != null ? this.inv.getItemStack().getHoverName() : Component.empty();
    }

}
