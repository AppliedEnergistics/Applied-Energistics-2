package appeng.hotkeys;

import java.util.function.Predicate;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import appeng.api.features.HotkeyAction;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;

public record InventoryHotkeyAction(Predicate<ItemStack> locatable, Opener opener) implements HotkeyAction {

    public InventoryHotkeyAction(ItemLike item, Opener opener) {
        this((stack) -> stack.is(item.asItem()), opener);
    }

    @Override
    public boolean run(Player player) {
        var items = player.getInventory().getNonEquipmentItems();
        for (int i = 0; i < items.size(); i++) {
            if (locatable.test(items.get(i))) {
                if (opener.open(player, MenuLocators.forInventorySlot(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface Opener {
        boolean open(Player player, ItemMenuHostLocator locator);
    }
}
