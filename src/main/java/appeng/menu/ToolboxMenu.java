package appeng.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.items.contents.NetworkToolMenuHost;
import appeng.items.tools.NetworkToolItem;
import appeng.menu.locator.MenuItemLocator;
import appeng.menu.slot.RestrictedInputSlot;

/**
 * Helper class for dealing with an equipped toolbox.
 */
public class ToolboxMenu {
    private final AEBaseMenu menu;
    private final MenuItemLocator locator;
    private final NetworkToolMenuHost inv;

    public ToolboxMenu(AEBaseMenu menu) {
        this.menu = menu;

        this.inv = NetworkToolItem.findNetworkToolInv(menu.getPlayer());
        if (inv != null) {
            this.locator = inv.getLocator();
            Integer slot = inv.getSlot();
            if (slot != null) {
                menu.lockPlayerInventorySlot(slot);
            }
        } else {
            this.locator = null;
        }

        // Add quick access slots for the upgrade cards stored in the toolbox
        if (isPresent()) {
            for (int i = 0; i < 9; i++) {
                var slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES,
                        this.inv.getInternalInventory(), i);
                // The toolbox is in the network tool that is part of the player inventory
                menu.addSlot(slot, SlotSemantics.TOOLBOX);
            }
        }

    }

    public boolean isPresent() {
        return this.inv != null;
    }

    public void tick() {
        if (isPresent()) {
            var currentItem = locator.locateItem(inv.getPlayer());

            if (currentItem != inv.getItemStack()) {
                if (!currentItem.isEmpty()) {
                    if (ItemStack.isSameItem(inv.getItemStack(), currentItem)) {
                        if (!locator.setItem(inv.getPlayer(), inv.getItemStack())) {
                            menu.setValidMenu(false);
                        }
                    } else {
                        menu.setValidMenu(false);
                    }
                } else {
                    menu.setValidMenu(false);
                }
            }
        }
    }

    public Component getName() {
        return this.inv != null ? this.inv.getItemStack().getHoverName() : Component.empty();
    }

}
