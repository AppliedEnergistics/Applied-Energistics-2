package appeng.menu;

import java.util.Objects;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.items.contents.PatternBoxMenuHost;
import appeng.items.tools.PatternBoxItem;
import appeng.menu.slot.RestrictedInputSlot;

public class PatternBoxMenu {
    private final AEBaseMenu menu;
    private final int slot;
    private final PatternBoxMenuHost inv;

    public PatternBoxMenu(AEBaseMenu menu) {
        this.menu = menu;

        this.inv = PatternBoxItem.findPatternBoxHost(menu.getPlayer());
        if (inv != null) {
            this.slot = Objects.requireNonNullElse(inv.getSlot(), 0);
            menu.lockPlayerInventorySlot(this.slot);
        } else {
            this.slot = 0;
        }

        // Add quick access slots for the stored patterns
        if (isPresent()) {
            for (int i = 0; i < 27; i++) {
                var slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ENCODED_PATTERN,
                        this.inv.getInventory(), i);
                // The patternbox is in the pattern box item that is part of the player inventory
                menu.addSlot(slot, SlotSemantics.PATTERNBOX);
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
                    if (ItemStack.isSameItem(inv.getItemStack(), currentItem)) {
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
}
