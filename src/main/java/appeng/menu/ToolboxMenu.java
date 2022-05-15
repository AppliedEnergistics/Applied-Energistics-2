package appeng.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.items.contents.NetworkToolMenuHost;
import appeng.items.tools.NetworkToolItem;
import appeng.menu.slot.RestrictedInputSlot;

/**
 * Helper class for dealing with an equipped toolbox.
 */
public class ToolboxMenu {
    private final AEBaseMenu menu;
    private int slot;
    private NetworkToolMenuHost inv;

    public ToolboxMenu(AEBaseMenu menu) {
        this.menu = menu;
        findToolbox(menu);

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
            var currentItem = menu.getPlayerInventory().getItem(slot);

            if (currentItem != inv.getItemStack()) {
                if (!currentItem.isEmpty()) {
                    if (ItemStack.isSame(inv.getItemStack(), currentItem)) {
                        menu.getPlayerInventory().setItem(slot, inv.getItemStack());
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

    private void findToolbox(AEBaseMenu menu) {
        var pi = menu.getPlayerInventory();
        for (int x = 0; x < pi.getContainerSize(); x++) {
            final ItemStack pii = pi.getItem(x);
            if (!pii.isEmpty() && pii.getItem() instanceof NetworkToolItem networkToolItem) {
                menu.lockPlayerInventorySlot(x);
                this.slot = x;
                this.inv = networkToolItem.getMenuHost(pi.player, x, pii, null);
                break;
            }
        }
    }

}
