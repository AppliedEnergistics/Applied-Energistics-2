package appeng.hotkeys;

import java.util.function.Predicate;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.features.HotkeyAction;

public class InventoryHotkeyAction implements HotkeyAction {

    private final Predicate<ItemStack> locatable;
    private final Opener opener;

    public InventoryHotkeyAction(Predicate<ItemStack> locatable, Opener opener) {
        this.locatable = locatable;
        this.opener = opener;
    }

    public InventoryHotkeyAction(Item item, Opener opener) {
        this((stack) -> stack.getItem() == item, opener);
    }

    @Override
    public boolean run(Player player) {
        var items = player.getInventory().items;
        for (int i = 0; i < items.size(); i++) {
            if (locatable.test(items.get(i))) {
                if (opener.open(player, i)) {
                    return true;
                }
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface Opener {
        boolean open(Player player, int inventorySlot);
    }
}
